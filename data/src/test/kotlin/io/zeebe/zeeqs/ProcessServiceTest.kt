package io.zeebe.zeeqs

import io.camunda.zeebe.model.bpmn.Bpmn
import io.zeebe.zeeqs.data.entity.BpmnElementType
import io.zeebe.zeeqs.data.entity.Process
import io.zeebe.zeeqs.data.repository.ProcessRepository
import io.zeebe.zeeqs.data.service.BpmnElementInfo
import io.zeebe.zeeqs.data.service.BpmnElementMetadata
import io.zeebe.zeeqs.data.service.ProcessService
import io.zeebe.zeeqs.data.service.UserTaskAssignmentDefinition
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.entry
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import java.time.Instant

@SpringBootTest
@TestConfiguration
class ProcessServiceTest(
        @Autowired val processService: ProcessService,
        @Autowired val processRepository: ProcessRepository
) {

    @Nested
    inner class BpmnInfoTests {

        @Test
        fun `should return element-id and element-name`() {
            // given
            val processDefinitionKey = 1L

            val bpmn = Bpmn.createExecutableProcess("process")
                    .startEvent("s").name("start")
                    .serviceTask("t").name("task")
                    .zeebeJobType("test")
                    .userTask("u").name("userTask")
                    .zeebeAssignee("user1").zeebeCandidateGroups("group1")
                    .endEvent("e").name("")
                    .done()

            processRepository.save(
                    Process(
                            key = processDefinitionKey,
                            bpmnProcessId = "process",
                            version = 1,
                            bpmnXML = Bpmn.convertToString(bpmn),
                            deployTime = Instant.now().toEpochMilli(),
                            resourceName = "process.bpmn",
                            checksum = "checksum"
                    )
            )

            // when
            val info = processService.getBpmnElementInfo(processDefinitionKey)

            // then
            assertThat(info)
                    .isNotNull()
                    .contains(entry("s", BpmnElementInfo("s", "start", BpmnElementType.START_EVENT, BpmnElementMetadata())))
                    .contains(entry("t", BpmnElementInfo("t", "task", BpmnElementType.SERVICE_TASK, BpmnElementMetadata(jobType = "test"))))
                    .contains(entry("u", BpmnElementInfo("u", "userTask", BpmnElementType.USER_TASK, BpmnElementMetadata(
                            userTaskAssignmentDefinition = UserTaskAssignmentDefinition(assignee = "user1", candidateGroups = "group1"))))
                    )
                    .contains(entry("e", BpmnElementInfo("e", null, BpmnElementType.END_EVENT, BpmnElementMetadata())))
        }

        @Test
        fun `should return nothing if process does not exist`() {
            // given
            val processDefinitionKey = 2L

            // when
            val info = processService.getBpmnElementInfo(processDefinitionKey)

            // then
            assertThat(info).isNullOrEmpty()
        }

    }

    @Nested
    inner class FormTests {

        private val processDefinitionKey = 1L
        private val formKey = "form_A"
        private val userForm = """{"x": 1}"""

        @BeforeEach
        fun `store process`() {
            val bpmn = Bpmn.createExecutableProcess("process")
                    .startEvent()
                    .userTask("A")
                    .zeebeUserTaskForm(formKey, userForm)
                    .endEvent()
                    .done()

            processRepository.save(
                    Process(
                            key = processDefinitionKey,
                            bpmnProcessId = "process",
                            version = 1,
                            bpmnXML = Bpmn.convertToString(bpmn),
                            deployTime = Instant.now().toEpochMilli(),
                            resourceName = "process.bpmn",
                            checksum = "checksum"
                    )
            )
        }

        @Test
        fun `should find by key`() {
            // when
            val form = processService.getForm(
                    processDefinitionKey = processDefinitionKey,
                    formKey = formKey)

            // then
            assertThat(form)
                    .isNotNull()
                    .isEqualTo(userForm)
        }

        @Test
        fun `should return nothing if the process definition doesn't exist`() {
            // when
            val form = processService.getForm(
                    processDefinitionKey = -1L,
                    formKey = formKey)

            // then
            assertThat(form).isNull()
        }

        @Test
        fun `should return nothing if form key doesn't exist`() {
            // when
            val form = processService.getForm(
                    processDefinitionKey = processDefinitionKey,
                    formKey = "other key")

            // then
            assertThat(form).isNull()
        }
    }


    @SpringBootApplication
    class TestConfiguration

}

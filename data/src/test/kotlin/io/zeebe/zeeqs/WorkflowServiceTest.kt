package io.zeebe.zeeqs

import io.camunda.zeebe.model.bpmn.Bpmn
import io.zeebe.zeeqs.data.entity.Workflow
import io.zeebe.zeeqs.data.repository.WorkflowRepository
import io.zeebe.zeeqs.data.service.BpmnElementInfo
import io.zeebe.zeeqs.data.service.WorkflowService
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.entry
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import java.time.Instant

@SpringBootTest
@TestConfiguration
class WorkflowServiceTest(
        @Autowired val workflowService: WorkflowService,
        @Autowired val workflowRepository: WorkflowRepository
) {

    @Test
    fun `should return element-id and element-name`() {
        // given
        val workflowKey = 1L

        val bpmn = Bpmn.createExecutableProcess("wf")
                .startEvent("s").name("start")
                .serviceTask("t").name("task")
                .zeebeJobType("test")
                .endEvent("e").name("")
                .done()

        workflowRepository.save(Workflow(
                key = workflowKey,
                bpmnProcessId = "wf",
                version = 1,
                bpmnXML = Bpmn.convertToString(bpmn),
                deployTime = Instant.now().toEpochMilli()
        ));

        // when
        val info = workflowService.getBpmnElementInfo(workflowKey)

        // then
        assertThat(info)
                .isNotNull()
                .contains(entry("s", BpmnElementInfo("s", "start")))
                .contains(entry("t", BpmnElementInfo("t", "task")))
                .contains(entry("e", BpmnElementInfo("e", null)))
    }

    @Test
    fun `should return nothing if workflow does not exist`() {
        // given
        val workflowKey = 2L

        // when
        val info = workflowService.getBpmnElementInfo(workflowKey)

        // then
        assertThat(info).isNullOrEmpty()
    }

    @SpringBootApplication
    class TestConfiguration

}

package io.zeebe.zeeqs

import io.camunda.zeebe.model.bpmn.Bpmn
import io.zeebe.zeeqs.data.entity.BpmnElementType
import io.zeebe.zeeqs.data.entity.Process
import io.zeebe.zeeqs.data.repository.ProcessRepository
import io.zeebe.zeeqs.data.service.BpmnElementInfo
import io.zeebe.zeeqs.data.service.BpmnElementMetadata
import io.zeebe.zeeqs.data.service.ProcessService
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
class ProcessServiceTest(
    @Autowired val processService: ProcessService,
    @Autowired val processRepository: ProcessRepository
) {

    @Test
    fun `should return element-id and element-name`() {
        // given
        val processDefinitionKey = 1L

        val bpmn = Bpmn.createExecutableProcess("process")
                .startEvent("s").name("start")
                .serviceTask("t").name("task")
                .zeebeJobType("test")
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

    @SpringBootApplication
    class TestConfiguration

}

package io.zeebe.zeeqs

import io.camunda.zeebe.client.ZeebeClient
import io.camunda.zeebe.model.bpmn.Bpmn
import io.zeebe.containers.ZeebeContainer
import io.zeebe.zeeqs.data.repository.ProcessRepository
import io.zeebe.zeeqs.importer.hazelcast.HazelcastImporter
import io.zeebe.zeeqs.importer.hazelcast.HazelcastProperties
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.context.SpringBootTest
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest
@Testcontainers
class HazelcastImporterProcessTest(
        @Autowired val importer: HazelcastImporter,
        @Autowired val processRepository: ProcessRepository) {

    private val hazelcastPort = 5701

    private lateinit var zeebeClient: ZeebeClient

    @Container
    var zeebe = ZeebeContainer(ZeebeTestcontainerUtil.ZEEBE_DOCKER_IMAGE)
            .withAdditionalExposedPort(hazelcastPort)

    @BeforeEach
    fun `start importer`() {
        val port = zeebe.getMappedPort(hazelcastPort)
        val hazelcastProperties = HazelcastProperties(
                "localhost:$port", "PT10S", "zeebe")
        importer.start(hazelcastProperties)
    }

    @BeforeEach
    fun `create Zeebe client`() {
        zeebeClient = ZeebeClient.newClientBuilder()
                .gatewayAddress(zeebe.externalGatewayAddress)
                .usePlaintext()
                .build()
    }

    @Test
    fun `should import process`() {
        // when
        zeebeClient.newDeployCommand()
                .addProcessModel(
                        Bpmn.createExecutableProcess("process")
                                .startEvent()
                                .serviceTask("task-1").zeebeJobType("test")
                                .endEvent()
                                .done(),
                        "process.bpmn")
                .send()
                .join()

        // then
        await.untilAsserted { assertThat(processRepository.findAll()).hasSize(1) }

        val process = processRepository.findAll().toList()[0]
        assertThat(process.key).isGreaterThan(0)
        assertThat(process.bpmnProcessId).isEqualTo("process")
        assertThat(process.version).isEqualTo(1)
        assertThat(process.deployTime).isGreaterThan(0)
        assertThat(process.bpmnXML).isNotEmpty()
        assertThat(process.resourceName).isEqualTo("process.bpmn")
        assertThat(process.checksum).isNotEmpty()
    }

    @SpringBootApplication
    class TestConfiguration
}


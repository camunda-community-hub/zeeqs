package io.zeebe.zeeqs

import io.zeebe.client.ZeebeClient
import io.zeebe.containers.ZeebeBrokerContainer
import io.zeebe.containers.ZeebePort
import io.zeebe.model.bpmn.Bpmn
import io.zeebe.zeeqs.data.repository.WorkflowRepository
import io.zeebe.zeeqs.importer.hazelcast.HazelcastImporter
import io.zeebe.zeeqs.importer.hazelcast.HazelcastProperties
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.context.SpringBootTest
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.MountableFile
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Duration
import java.time.Instant

@SpringBootTest
@Testcontainers
class HazelcastImporterTest(
        @Autowired val importer: HazelcastImporter,
        @Autowired val workflowRepository: WorkflowRepository) {

    val exporterJarPath: Path = Paths.get("../target/exporter/zeebe-hazelcast-exporter.jar")
    val containerPath = "/usr/local/zeebe/exporter/zeebe-hazelcast-exporter.jar"

    val hazelcastPort = 5701

    @Container
    var zeebe = ZeebeBrokerContainer("0.22.1")
            .withConfigurationResource("zeebe.cfg.toml")
            .withCopyFileToContainer(MountableFile.forHostPath(exporterJarPath), containerPath)
            .withExposedPorts(hazelcastPort)

    @BeforeEach
    fun init() {
        assertThat(exporterJarPath).exists()
    }

    @Test
    fun `should import workflow`() {
        // given
        val port = zeebe.getMappedPort(hazelcastPort)
        val hazelcastProperties = HazelcastProperties(
                "localhost:$port", "PT10S", "zeebe")
        importer.start(hazelcastProperties)

        val client = ZeebeClient.newClientBuilder()
                .brokerContactPoint(zeebe.getExternalAddress(ZeebePort.GATEWAY))
                .usePlaintext()
                .build()

        // when
        client.newDeployCommand()
                .addWorkflowModel(
                        Bpmn.createExecutableProcess("wf")
                                .startEvent()
                                .serviceTask("task-1").zeebeJobType("test")
                                .endEvent()
                                .done(),
                        "wf.bpmn")
                .send()
                .join()

        // verify
        waitUntilWorkflowIsImported()

        assertThat(workflowRepository.findAll()).hasSize(1)

        val workflow = workflowRepository.findAll().toList()[0]
        assertThat(workflow.key).isGreaterThan(0)
        assertThat(workflow.bpmnProcessId).isEqualTo("wf")
        assertThat(workflow.version).isEqualTo(1)
        assertThat(workflow.deployTime).isGreaterThan(0)
        assertThat(workflow.bpmnXML).isNotEmpty()
    }

    private fun waitUntilWorkflowIsImported() {
        val timeout = Instant.now().plus(Duration.ofSeconds(10))
        while (workflowRepository.count() < 1 && Instant.now().isBefore(timeout)) {
            Thread.sleep(10)
        }
    }

    @SpringBootApplication
    class TestConfiguration
}


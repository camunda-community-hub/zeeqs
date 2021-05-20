package io.zeebe.zeeqs

import io.camunda.zeebe.client.ZeebeClient
import io.zeebe.containers.ZeebeContainer
import io.camunda.zeebe.model.bpmn.Bpmn
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
import org.testcontainers.utility.MountableFile
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Duration
import java.time.Instant

@SpringBootTest
@Testcontainers
class HazelcastImporterTest(
        @Autowired val importer: HazelcastImporter,
        @Autowired val processRepository: ProcessRepository) {

    val exporterJarPath: Path = Paths.get("../target/exporter/zeebe-hazelcast-exporter.jar")
    val containerPath = "/usr/local/zeebe/exporter/zeebe-hazelcast-exporter.jar"

    val hazelcastPort = 5701

    @Container
    var zeebe = ZeebeContainer()
        .withEnv("ZEEBE_BROKER_EXPORTERS_HAZELCAST_CLASSNAME", "io.zeebe.hazelcast.exporter.HazelcastExporter")
        .withEnv("ZEEBE_BROKER_EXPORTERS_HAZELCAST_JARPATH", "exporter/zeebe-hazelcast-exporter.jar")
        .withCopyFileToContainer(MountableFile.forHostPath(exporterJarPath), containerPath)
        .withAdditionalExposedPort(hazelcastPort)

    @BeforeEach
    fun init() {
        assertThat(exporterJarPath).exists()
    }

    @Test
    fun `should import process`() {
        // given
        val port = zeebe.getMappedPort(hazelcastPort)
        val hazelcastProperties = HazelcastProperties(
                "localhost:$port", "PT10S", "zeebe")
        importer.start(hazelcastProperties)

        val client = ZeebeClient.newClientBuilder()
                .gatewayAddress(zeebe.externalGatewayAddress)
                .usePlaintext()
                .build()

        // when
        client.newDeployCommand()
                .addProcessModel(
                        Bpmn.createExecutableProcess("process")
                                .startEvent()
                                .serviceTask("task-1").zeebeJobType("test")
                                .endEvent()
                                .done(),
                        "process.bpmn")
                .send()
                .join()

        // verify
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

    private fun waitUntilProcessIsImported() {
        val timeout = Instant.now().plus(Duration.ofSeconds(10))
        while (processRepository.count() < 1 && Instant.now().isBefore(timeout)) {
            Thread.sleep(10)
        }
    }

    @SpringBootApplication
    class TestConfiguration
}


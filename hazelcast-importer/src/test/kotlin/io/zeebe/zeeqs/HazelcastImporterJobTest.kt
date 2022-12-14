package io.zeebe.zeeqs

import io.camunda.zeebe.client.ZeebeClient
import io.camunda.zeebe.model.bpmn.Bpmn
import io.zeebe.containers.ZeebeContainer
import io.zeebe.zeeqs.data.entity.JobState
import io.zeebe.zeeqs.data.entity.UserTaskState
import io.zeebe.zeeqs.data.repository.JobRepository
import io.zeebe.zeeqs.data.repository.UserTaskRepository
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
import javax.transaction.Transactional

@SpringBootTest
@Testcontainers
@Transactional
class HazelcastImporterJobTest(
        @Autowired val importer: HazelcastImporter,
        @Autowired val jobRepository: JobRepository,
        @Autowired val userTaskRepository: UserTaskRepository) {


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
    fun `should import job`() {
        // given
        zeebeClient.newDeployCommand()
                .addProcessModel(
                        Bpmn.createExecutableProcess("process")
                                .startEvent()
                                .serviceTask("A").zeebeJobType("A")
                                .endEvent()
                                .done(),
                        "process.bpmn")
                .send()
                .join()

        // when
        val processInstance = zeebeClient.newCreateInstanceCommand()
                .bpmnProcessId("process")
                .latestVersion()
                .send()
                .join()

        // then
        await.untilAsserted { assertThat(jobRepository.findAll()).hasSize(1) }

        val job = jobRepository.findAll().toList()[0]
        assertThat(job.key).isPositive()
        assertThat(job.jobType).isEqualTo("A")
        assertThat(job.state).isEqualTo(JobState.ACTIVATABLE)
        assertThat(job.processInstanceKey).isEqualTo(processInstance.processInstanceKey)
        assertThat(job.elementInstanceKey).isPositive()
        assertThat(job.startTime).isPositive()
        assertThat(job.endTime).isNull()
    }

    @Test
    fun `should import user task`() {
        // given
        zeebeClient.newDeployCommand()
                .addProcessModel(
                        Bpmn.createExecutableProcess("process")
                                .startEvent()
                                .userTask("A")
                                .zeebeAssignee("test")
                                .zeebeCandidateGroups("test-group")
                                .zeebeUserTaskForm("form_A", """{"x": 1}""")
                                .endEvent()
                                .done(),
                        "process.bpmn")
                .send()
                .join()

        // when
        val processInstance = zeebeClient.newCreateInstanceCommand()
                .bpmnProcessId("process")
                .latestVersion()
                .send()
                .join()

        // then
        await.untilAsserted { assertThat(userTaskRepository.findAll()).hasSize(1) }

        val userTask = userTaskRepository.findAll().toList()[0]
        assertThat(userTask.key).isPositive()
        assertThat(userTask.state).isEqualTo(UserTaskState.CREATED)
        assertThat(userTask.processInstanceKey).isEqualTo(processInstance.processInstanceKey)
        assertThat(userTask.processDefinitionKey).isEqualTo(processInstance.processDefinitionKey)
        assertThat(userTask.elementInstanceKey).isPositive()
        assertThat(userTask.startTime).isPositive()
        assertThat(userTask.endTime).isNull()
        assertThat(userTask.assignee).isEqualTo("test")
        assertThat(userTask.candidateGroups).isEqualTo("""["test-group"]""")
        assertThat(userTask.formKey).isEqualTo("camunda-forms:bpmn:form_A")
    }

    @SpringBootApplication
    class TestConfiguration
}


package io.zeebe.zeeqs

import io.camunda.zeebe.client.ZeebeClient
import io.camunda.zeebe.model.bpmn.Bpmn
import io.zeebe.containers.ZeebeContainer
import io.zeebe.zeeqs.data.entity.DecisionEvaluationState
import io.zeebe.zeeqs.data.repository.DecisionEvaluationRepository
import io.zeebe.zeeqs.data.repository.DecisionRepository
import io.zeebe.zeeqs.data.repository.DecisionRequirementsRepository
import io.zeebe.zeeqs.data.repository.EvaluatedDecisionRepository
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
class HazelcastImporterDecisionTest(
    @Autowired val importer: HazelcastImporter,
    @Autowired val decisionRepository: DecisionRepository,
    @Autowired val decisionRequirementsRepository: DecisionRequirementsRepository,
    @Autowired val decisionEvaluationRepository: DecisionEvaluationRepository,
    @Autowired val evaluatedDecisionRepository: EvaluatedDecisionRepository
) {


    private val hazelcastPort = 5701

    private lateinit var zeebeClient: ZeebeClient

    @Container
    var zeebe = ZeebeContainer(ZeebeTestcontainerUtil.ZEEBE_DOCKER_IMAGE)
        .withAdditionalExposedPort(hazelcastPort)

    @BeforeEach
    fun `start importer`() {
        val port = zeebe.getMappedPort(hazelcastPort)
        val hazelcastProperties = HazelcastProperties(
            "localhost:$port", "PT10S", "zeebe"
        )
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
    fun `should import decision`() {
        // when
        zeebeClient.newDeployResourceCommand()
            .addResourceFromClasspath("rating.dmn")
            .send()
            .join()

        // then
        await.untilAsserted { assertThat(decisionRepository.findAll()).hasSize(2) }

        val decisionA = decisionRepository.findAll().first { it.decisionId == "decision_a" }
        assertThat(decisionA).isNotNull
        assertThat(decisionA.key).isPositive()
        assertThat(decisionA.decisionId).isEqualTo("decision_a")
        assertThat(decisionA.decisionName).isEqualTo("Decision A")
        assertThat(decisionA.decisionRequirementsId).isEqualTo("Ratings")
        assertThat(decisionA.version).isEqualTo(1)
        assertThat(decisionA.decisionRequirementsKey).isPositive()

        val decisionB = decisionRepository.findAll().first { it.decisionId == "decision_b" }
        assertThat(decisionB).isNotNull
        assertThat(decisionB.key).isPositive()
        assertThat(decisionB.decisionId).isEqualTo("decision_b")
        assertThat(decisionB.decisionName).isEqualTo("Decision B")
        assertThat(decisionB.decisionRequirementsId).isEqualTo("Ratings")
        assertThat(decisionB.version).isEqualTo(1)
        assertThat(decisionB.decisionRequirementsKey).isPositive()
    }

    @Test
    fun `should import decision requirements`() {
        // when
        zeebeClient.newDeployResourceCommand()
            .addResourceFromClasspath("rating.dmn")
            .send()
            .join()

        // then
        await.untilAsserted { assertThat(decisionRequirementsRepository.findAll()).hasSize(1) }

        val decisionRequirements = decisionRequirementsRepository.findAll().first()
        assertThat(decisionRequirements.key).isPositive()
        assertThat(decisionRequirements.decisionRequirementsId).isEqualTo("Ratings")
        assertThat(decisionRequirements.decisionRequirementsName).isEqualTo("DRD")
        assertThat(decisionRequirements.namespace).isEqualTo("http://camunda.org/schema/1.0/dmn")
        assertThat(decisionRequirements.version).isEqualTo(1)
        assertThat(decisionRequirements.deployTime).isPositive()
        assertThat(decisionRequirements.resourceName).isEqualTo("rating.dmn")
        assertThat(decisionRequirements.dmnXML).isNotEmpty()
        assertThat(decisionRequirements.checksum).isNotEmpty()
    }

    @Test
    fun `should import decision evaluation`() {
        // given
        zeebeClient.newDeployResourceCommand()
            .addResourceFromClasspath("rating.dmn")
            .addProcessModel(Bpmn.createExecutableProcess("process")
                .startEvent()
                .businessRuleTask("task") {
                    it.zeebeCalledDecisionId("decision_a")
                        .zeebeResultVariable("rating")
                }
                .endEvent()
                .done(), "process.bpmn")
            .send()
            .join()

        // when
        val processInstanceKey = zeebeClient.newCreateInstanceCommand()
            .bpmnProcessId("process")
            .latestVersion()
            .variables(mapOf("x" to 7))
            .send()
            .join()
            .processInstanceKey

        // then
        await.untilAsserted { assertThat(decisionEvaluationRepository.findAll()).hasSize(1) }

        val decisionEvaluation = decisionEvaluationRepository.findAll().first()

        assertThat(decisionEvaluation.key).isPositive()
        assertThat(decisionEvaluation.decisionOutput).isEqualTo("\"A+\"")
        assertThat(decisionEvaluation.state).isEqualTo(DecisionEvaluationState.EVALUATED)
        assertThat(decisionEvaluation.processInstanceKey).isEqualTo(processInstanceKey)

        assertThat(evaluatedDecisionRepository.findAllByDecisionEvaluationKey(decisionEvaluation.key))
            .hasSize(2)
    }

    @SpringBootApplication
    class TestConfiguration
}


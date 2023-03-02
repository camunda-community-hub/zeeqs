package io.zeebe.zeeqs.importer.hazelcast

import io.zeebe.exporter.proto.Schema
import io.zeebe.zeeqs.data.entity.*
import io.zeebe.zeeqs.data.reactive.DataUpdatesPublisher
import io.zeebe.zeeqs.data.repository.*
import org.springframework.stereotype.Component

@Component
class HazelcastDecisionImporter(
    private val dataUpdatesPublisher: DataUpdatesPublisher,
    private val decisionRepository: DecisionRepository,
    private val decisionRequirementsRepository: DecisionRequirementsRepository,
    private val decisionEvaluationRepository: DecisionEvaluationRepository,
    private val evaluatedDecisionRepository: EvaluatedDecisionRepository,
    private val decisionEvaluationInputRepository: DecisionEvaluationInputRepository,
    private val decisionEvaluationOutputRepository: DecisionEvaluationOutputRepository
) {

    fun importDecision(decision: Schema.DecisionRecord) {
        val entity = decisionRepository
            .findById(decision.decisionKey)
            .orElse(createDecision(decision))

        decisionRepository.save(entity)

        dataUpdatesPublisher.onDecisionUpdated(entity)
    }

    private fun createDecision(decision: Schema.DecisionRecord): Decision {
        return Decision(
            key = decision.decisionKey,
            decisionId = decision.decisionId,
            decisionName = decision.decisionName,
            version = decision.version,
            decisionRequirementsKey = decision.decisionRequirementsKey,
            decisionRequirementsId = decision.decisionRequirementsId
        )
    }

    fun importDecisionRequirements(decisionRequirements: Schema.DecisionRequirementsRecord) {
        val entity = decisionRequirementsRepository
            .findById(decisionRequirements.decisionRequirementsMetadata.decisionRequirementsKey)
            .orElse(createDecisionRequirements(decisionRequirements))

        decisionRequirementsRepository.save(entity)
    }

    private fun createDecisionRequirements(decisionRequirements: Schema.DecisionRequirementsRecord): DecisionRequirements {
        val metadata = decisionRequirements.decisionRequirementsMetadata
        return DecisionRequirements(
            key = metadata.decisionRequirementsKey,
            decisionRequirementsId = metadata.decisionRequirementsId,
            decisionRequirementsName = metadata.decisionRequirementsName,
            version = metadata.decisionRequirementsVersion,
            namespace = metadata.namespace,
            dmnXML = decisionRequirements.resource.toStringUtf8(),
            deployTime = decisionRequirements.metadata.timestamp,
            resourceName = metadata.resourceName,
            checksum = metadata.checksum.toStringUtf8()
        )
    }

    fun importDecisionEvaluation(decisionEvaluation: Schema.DecisionEvaluationRecord) {
        val decisionEvaluationKey = decisionEvaluation.metadata.key

        val entity = decisionEvaluationRepository
            .findById(decisionEvaluationKey)
            .orElse(createDecisionEvaluation(decisionEvaluation))

        decisionEvaluationRepository.save(entity)

        decisionEvaluation.evaluatedDecisionsList.forEachIndexed { index, evaluatedDecision ->
            val decisionKey = evaluatedDecision.decisionKey
            // a decision can be evaluated multiple times
            val evaluatedDecisionId =
                "$decisionEvaluationKey-$decisionKey-$index"

            evaluatedDecisionRepository.save(
                EvaluatedDecision(
                    id = evaluatedDecisionId,
                    decisionKey = decisionKey,
                    decisionOutput = evaluatedDecision.decisionOutput,
                    decisionEvaluationKey = decisionEvaluationKey
                )
            )

            evaluatedDecision.evaluatedInputsList.forEach {
                decisionEvaluationInputRepository.save(
                    DecisionEvaluationInput(
                        id = "$evaluatedDecisionId-${it.inputId}",
                        inputId = it.inputId,
                        inputName = it.inputName,
                        value = it.inputValue,
                        evaluatedDecisionId = evaluatedDecisionId
                    )
                )
            }

            evaluatedDecision.matchedRulesList.forEach { matchedRule ->
                val ruleIndex = matchedRule.ruleIndex

                matchedRule.evaluatedOutputsList.forEach {
                    decisionEvaluationOutputRepository.save(
                        DecisionEvaluationOutput(
                            id = "$evaluatedDecisionId-$ruleIndex-${it.outputId}",
                            outputId = it.outputId,
                            outputName = it.outputName,
                            value = it.outputValue,
                            evaluatedDecisionId = evaluatedDecisionId,
                            ruleId = matchedRule.ruleId,
                            ruleIndex = ruleIndex
                        )
                    )
                }
            }
        }

        dataUpdatesPublisher.onDecisionEvaluationUpdated(entity)
    }

    private fun createDecisionEvaluation(decisionEvaluation: Schema.DecisionEvaluationRecord): DecisionEvaluation {
        val metadata = decisionEvaluation.metadata

        return DecisionEvaluation(
            key = metadata.key,
            decisionKey = decisionEvaluation.decisionKey,
            decisionRequirementsKey = decisionEvaluation.decisionRequirementsKey,
            decisionOutput = decisionEvaluation.decisionOutput,
            state = when (metadata.intent) {
                "EVALUATED" -> DecisionEvaluationState.EVALUATED
                "FAILED" -> DecisionEvaluationState.FAILED
                else -> DecisionEvaluationState.EVALUATED
            },
            evaluationTime = metadata.timestamp,
            failedDecisionId = decisionEvaluation.failedDecisionId.takeIf { it.isNotEmpty() },
            evaluationFailureMessage = decisionEvaluation.evaluationFailureMessage.takeIf { it.isNotEmpty() },
            processInstanceKey = decisionEvaluation.processInstanceKey.takeIf { it > 0 },
            elementInstanceKey = decisionEvaluation.elementInstanceKey.takeIf { it > 0 }
        )
    }


}
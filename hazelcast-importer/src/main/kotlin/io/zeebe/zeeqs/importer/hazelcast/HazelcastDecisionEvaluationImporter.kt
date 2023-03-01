package io.zeebe.zeeqs.importer.hazelcast

import io.zeebe.exporter.proto.Schema
import io.zeebe.zeeqs.data.entity.*
import io.zeebe.zeeqs.data.repository.DecisionEvaluationInputRepository
import io.zeebe.zeeqs.data.repository.DecisionEvaluationOutputRepository
import io.zeebe.zeeqs.data.repository.DecisionEvaluationRepository
import io.zeebe.zeeqs.data.repository.EvaluatedDecisionRepository
import org.springframework.stereotype.Component

@Component
class HazelcastDecisionEvaluationImporter(
    private val decisionEvaluationRepository: DecisionEvaluationRepository,
    private val evaluatedDecisionRepository: EvaluatedDecisionRepository,
    private val decisionEvaluationInputRepository: DecisionEvaluationInputRepository,
    private val decisionEvaluationOutputRepository: DecisionEvaluationOutputRepository
) {

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
    }

    private fun createDecisionEvaluation(decisionEvaluation: Schema.DecisionEvaluationRecord): DecisionEvaluation {
        val metadata = decisionEvaluation.metadata

        return DecisionEvaluation(
            key = metadata.key,
            decisionKey = decisionEvaluation.decisionKey,
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
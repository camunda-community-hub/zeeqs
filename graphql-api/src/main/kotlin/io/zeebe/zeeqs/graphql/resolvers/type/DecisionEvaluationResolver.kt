package io.zeebe.zeeqs.graphql.resolvers.type

import io.zeebe.zeeqs.data.entity.*
import io.zeebe.zeeqs.data.repository.*
import org.springframework.data.repository.findByIdOrNull
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class DecisionEvaluationResolver(
    private val evaluatedDecisionRepository: EvaluatedDecisionRepository,
    private val decisionEvaluationInputRepository: DecisionEvaluationInputRepository,
    private val decisionEvaluationOutputRepository: DecisionEvaluationOutputRepository,
    private val decisionRepository: DecisionRepository,
    private val processInstanceRepository: ProcessInstanceRepository,
    private val elementInstanceRepository: ElementInstanceRepository
) {

    @SchemaMapping(typeName = "DecisionEvaluation", field = "decision")
    fun decision(decisionEvaluation: DecisionEvaluation): Decision? {
        return decisionRepository.findByIdOrNull(decisionEvaluation.decisionKey)
    }

    @SchemaMapping(typeName = "DecisionEvaluation", field = "failedDecision")
    fun failedDecision(decisionEvaluation: DecisionEvaluation): Decision? {
        return decisionEvaluation.failedDecisionId?.let {
            decisionRepository.findByDecisionRequirementsKeyAndDecisionId(
                decisionRequirementsKey = decisionEvaluation.decisionRequirementsKey,
                decisionId = it
            )
        }
    }

    @SchemaMapping(typeName = "DecisionEvaluation", field = "processInstance")
    fun processInstance(decisionEvaluation: DecisionEvaluation): ProcessInstance? {
        return decisionEvaluation.processInstanceKey?.let {
            processInstanceRepository.findByIdOrNull(it)
        }
    }

    @SchemaMapping(typeName = "DecisionEvaluation", field = "elementInstance")
    fun elementInstance(decisionEvaluation: DecisionEvaluation): ElementInstance? {
        return decisionEvaluation.elementInstanceKey?.let {
            elementInstanceRepository.findByIdOrNull(it)
        }
    }

    @SchemaMapping(typeName = "DecisionEvaluation", field = "evaluationTime")
    fun evaluationTime(
        decisionEvaluation: DecisionEvaluation,
        @Argument zoneId: String
    ): String? {
        return decisionEvaluation.evaluationTime.let {
            ResolverExtension.timestampToString(
                it,
                zoneId
            )
        }
    }

    @SchemaMapping(typeName = "DecisionEvaluation", field = "evaluatedDecisions")
    fun evaluatedDecisions(decisionEvaluation: DecisionEvaluation): List<EvaluatedDecision> {
        return evaluatedDecisionRepository.findAllByDecisionEvaluationKey(
            decisionEvaluationKey = decisionEvaluation.key
        )
    }

    @SchemaMapping(typeName = "EvaluatedDecision", field = "decision")
    fun evaluatedDecision(evaluatedDecision: EvaluatedDecision): Decision? {
        return decisionRepository.findByIdOrNull(evaluatedDecision.decisionKey)
    }

    @SchemaMapping(typeName = "EvaluatedDecision", field = "inputs")
    fun evaluatedInputs(evaluatedDecision: EvaluatedDecision): List<DecisionEvaluationInput> {
        return decisionEvaluationInputRepository.findAllByEvaluatedDecisionId(
            evaluatedDecisionId = evaluatedDecision.id
        )
    }

    @SchemaMapping(typeName = "EvaluatedDecision", field = "matchedRules")
    fun matchedRules(evaluatedDecision: EvaluatedDecision): List<DecisionEvaluationMatchedRule> {
        return decisionEvaluationOutputRepository.findAllByEvaluatedDecisionId(
            evaluatedDecisionId = evaluatedDecision.id
        )
            .groupBy { MatchedRule(ruleId = it.ruleId, ruleIndex = it.ruleIndex) }
            .map { (rule, outputs) ->
                DecisionEvaluationMatchedRule(
                    ruleId = rule.ruleId,
                    ruleIndex = rule.ruleIndex,
                    outputs = outputs
                )
            }
    }

    data class MatchedRule(
        val ruleId: String,
        val ruleIndex: Int
    )

    data class DecisionEvaluationMatchedRule(
        val ruleId: String,
        val ruleIndex: Int,
        val outputs: List<DecisionEvaluationOutput>
    )

}
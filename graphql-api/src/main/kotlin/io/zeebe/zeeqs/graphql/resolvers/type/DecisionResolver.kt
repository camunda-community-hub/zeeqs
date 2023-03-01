package io.zeebe.zeeqs.graphql.resolvers.type

import io.zeebe.zeeqs.data.entity.Decision
import io.zeebe.zeeqs.data.entity.DecisionRequirements
import io.zeebe.zeeqs.data.repository.DecisionEvaluationRepository
import io.zeebe.zeeqs.data.repository.DecisionRequirementsRepository
import io.zeebe.zeeqs.graphql.resolvers.connection.DecisionEvaluationConnection
import org.springframework.data.repository.findByIdOrNull
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class DecisionResolver(
    private val decisionRequirementsRepository: DecisionRequirementsRepository,
    private val decisionEvaluationRepository: DecisionEvaluationRepository
) {

    @SchemaMapping(typeName = "Decision", field = "decisionRequirements")
    fun decisionRequirements(decision: Decision): DecisionRequirements? {
        return decisionRequirementsRepository.findByIdOrNull(decision.decisionRequirementsKey)
    }

    @SchemaMapping(typeName = "Decision", field = "evaluations")
    fun evaluations(decision: Decision): DecisionEvaluationConnection {
        return DecisionEvaluationConnection(
            getItems = { decisionEvaluationRepository.findAllByDecisionKey(decisionKey = decision.key) },
            getCount = { decisionEvaluationRepository.countByDecisionKey(decisionKey = decision.key) }
        )
    }

}
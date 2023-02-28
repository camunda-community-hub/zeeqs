package io.zeebe.zeeqs.graphql.resolvers.type

import io.zeebe.zeeqs.data.entity.Decision
import io.zeebe.zeeqs.data.entity.DecisionRequirements
import io.zeebe.zeeqs.data.repository.DecisionRequirementsRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class DecisionResolver(
    private val decisionRequirementsRepository: DecisionRequirementsRepository
) {

    @SchemaMapping(typeName = "Decision", field = "decisionRequirements")
    fun decisionRequirements(decision: Decision): DecisionRequirements? {
        return decisionRequirementsRepository.findByIdOrNull(decision.decisionRequirementsKey)
    }

}
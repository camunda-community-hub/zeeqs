package io.zeebe.zeeqs.graphql.resolvers.type

import io.zeebe.zeeqs.data.entity.Decision
import io.zeebe.zeeqs.data.entity.DecisionRequirements
import io.zeebe.zeeqs.data.repository.DecisionRepository
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class DecisionRequirementsResolver(
    private val decisionRepository: DecisionRepository
) {

    @SchemaMapping(typeName = "DecisionRequirements", field = "deployTime")
    fun deployTime(
        decisionRequirements: DecisionRequirements,
        @Argument zoneId: String
    ): String? {
        return decisionRequirements.deployTime.let {
            ResolverExtension.timestampToString(
                it,
                zoneId
            )
        }
    }

    @SchemaMapping(typeName = "DecisionRequirements", field = "decisions")
    fun decisions(decisionRequirements: DecisionRequirements): List<Decision> {
        return decisionRepository.findAllByDecisionRequirementsKey(
            decisionRequirementsKey = decisionRequirements.key
        )
    }

}
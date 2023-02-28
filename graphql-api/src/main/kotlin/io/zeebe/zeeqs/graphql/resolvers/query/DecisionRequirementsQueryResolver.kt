package io.zeebe.zeeqs.graphql.resolvers.query

import io.zeebe.zeeqs.data.entity.DecisionRequirements
import io.zeebe.zeeqs.data.repository.DecisionRequirementsRepository
import io.zeebe.zeeqs.graphql.resolvers.connection.DecisionRequirementsConnection
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class DecisionRequirementsQueryResolver(
    private val decisionRequirementsRepository: DecisionRequirementsRepository
) {

    @QueryMapping
    fun decisionRequirements(
        @Argument perPage: Int,
        @Argument page: Int
    ): DecisionRequirementsConnection {
        return DecisionRequirementsConnection(
            getItems = {
                decisionRequirementsRepository.findAll(PageRequest.of(page, perPage)).toList()
            },
            getCount = { decisionRequirementsRepository.count() }
        )
    }

    @QueryMapping
    fun decisionRequirement(@Argument key: Long): DecisionRequirements? {
        return decisionRequirementsRepository.findByIdOrNull(key)
    }

}
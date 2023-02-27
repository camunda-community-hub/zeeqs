package io.zeebe.zeeqs.graphql.resolvers.query

import io.zeebe.zeeqs.data.entity.Decision
import io.zeebe.zeeqs.data.repository.DecisionRepository
import io.zeebe.zeeqs.graphql.resolvers.connection.DecisionConnection
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class DecisionQueryResolver(
    private val decisionRepository: DecisionRepository
) {

    @QueryMapping
    fun decisions(
        @Argument perPage: Int,
        @Argument page: Int
    ): DecisionConnection {
        return DecisionConnection(
            getItems = { decisionRepository.findAll(PageRequest.of(page, perPage)).toList() },
            getCount = { decisionRepository.count() }
        )
    }

    @QueryMapping
    fun decision(@Argument key: Long): Decision? {
        return decisionRepository.findByIdOrNull(key)
    }

}
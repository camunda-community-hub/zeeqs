package io.zeebe.zeeqs.graphql.resolvers.query

import io.zeebe.zeeqs.data.entity.DecisionEvaluation
import io.zeebe.zeeqs.data.entity.DecisionEvaluationState
import io.zeebe.zeeqs.data.repository.DecisionEvaluationRepository
import io.zeebe.zeeqs.graphql.resolvers.connection.DecisionEvaluationConnection
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class DecisionEvaluationQueryResolver(
    private val decisionEvaluationRepository: DecisionEvaluationRepository
) {

    @QueryMapping
    fun decisionEvaluations(
        @Argument perPage: Int,
        @Argument page: Int,
        @Argument stateIn: List<DecisionEvaluationState>
    ): DecisionEvaluationConnection {
        return DecisionEvaluationConnection(
            getItems = {
                decisionEvaluationRepository.findByStateIn(
                    stateIn,
                    PageRequest.of(page, perPage)
                )
            },
            getCount = { decisionEvaluationRepository.countByStateIn(stateIn) }
        )
    }

    @QueryMapping
    fun decisionEvaluation(@Argument key: Long): DecisionEvaluation? {
        return decisionEvaluationRepository.findByIdOrNull(key)
    }

}
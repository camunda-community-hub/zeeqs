package io.zeebe.zeeqs.graphql.resolvers.subscription

import io.zeebe.zeeqs.data.entity.DecisionEvaluation
import io.zeebe.zeeqs.data.reactive.DataUpdatesSubscription
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.SubscriptionMapping
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux

@Controller
class DecisionEvaluationSubscriptionMapping(private val subscription: DataUpdatesSubscription) {

    @SubscriptionMapping
    fun decisionEvaluationUpdates(
        @Argument filter: DecisionEvaluationUpdateFilter?
    ): Flux<DecisionEvaluation> {
        return subscription.decisionEvaluationSubscription()
            .filter {
                filter == null || (
                        (filter.decisionKey == null || filter.decisionKey == it.decisionKey)
                                && (filter.decisionRequirementsKey == null || filter.decisionRequirementsKey == it.decisionRequirementsKey)
                        )
            }
    }

    data class DecisionEvaluationUpdateFilter(
        val decisionKey: Long?,
        val decisionRequirementsKey: Long?
    )

}
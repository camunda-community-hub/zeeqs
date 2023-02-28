package io.zeebe.zeeqs.graphql.resolvers.subscription

import io.zeebe.zeeqs.data.entity.Decision
import io.zeebe.zeeqs.data.reactive.DataUpdatesSubscription
import org.springframework.graphql.data.method.annotation.SubscriptionMapping
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux

@Controller
class DecisionSubscriptionMapping(
    private val subscription: DataUpdatesSubscription
) {

    @SubscriptionMapping
    fun decisionUpdates(): Flux<Decision> {
        return subscription.decisionSubscription()
    }
}
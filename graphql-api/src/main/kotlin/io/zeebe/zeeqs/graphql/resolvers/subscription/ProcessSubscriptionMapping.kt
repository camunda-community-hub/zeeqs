package io.zeebe.zeeqs.graphql.resolvers.subscription

import io.zeebe.zeeqs.data.entity.Process
import io.zeebe.zeeqs.data.reactive.DataUpdatesSubscription
import org.springframework.graphql.data.method.annotation.SubscriptionMapping
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux

@Controller
class ProcessSubscriptionMapping(
        private val subscription: DataUpdatesSubscription
) {

    @SubscriptionMapping
    fun processUpdates(): Flux<Process> {
        return subscription.processSubscription()
    }
}
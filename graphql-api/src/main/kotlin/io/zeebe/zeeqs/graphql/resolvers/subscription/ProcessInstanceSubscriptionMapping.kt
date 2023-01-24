package io.zeebe.zeeqs.graphql.resolvers.subscription

import io.zeebe.zeeqs.data.reactive.DataUpdatesSubscription
import io.zeebe.zeeqs.data.reactive.ProcessInstanceUpdate
import io.zeebe.zeeqs.data.reactive.ProcessInstanceUpdateType
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.SubscriptionMapping
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux

@Controller
class ProcessInstanceSubscriptionMapping(private val subscription: DataUpdatesSubscription) {

    @SubscriptionMapping
    fun processInstanceUpdates(
            @Argument filter: ProcessInstanceUpdateFilter?): Flux<ProcessInstanceUpdate> {
        return subscription.processInstanceUpdateSubscription()
                .filter {
                    filter == null || (
                            (filter.processKey == null || filter.processKey == it.processKey)
                                    && (filter.processInstanceKey == null || filter.processInstanceKey == it.processInstanceKey)
                                    && (filter.updateTypeIn.isEmpty() || filter.updateTypeIn.contains(it.updateType))
                            )
                }
    }

    data class ProcessInstanceUpdateFilter(
            val processKey: Long?,
            val processInstanceKey: Long?,
            val updateTypeIn: List<ProcessInstanceUpdateType> = emptyList()
    )

}
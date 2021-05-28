package io.zeebe.zeeqs.graphql.resolvers.type

import graphql.kickstart.tools.GraphQLResolver
import io.zeebe.zeeqs.data.entity.MessageSubscription
import io.zeebe.zeeqs.data.entity.Timer
import io.zeebe.zeeqs.data.entity.Process
import io.zeebe.zeeqs.data.entity.ProcessInstanceState
import io.zeebe.zeeqs.data.repository.MessageSubscriptionRepository
import io.zeebe.zeeqs.data.repository.TimerRepository
import io.zeebe.zeeqs.data.repository.ProcessInstanceRepository
import io.zeebe.zeeqs.graphql.resolvers.connection.ProcessInstanceConnection
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component

@Component
class ProcessResolver(
    val processInstanceRepository: ProcessInstanceRepository,
    val timerRepository: TimerRepository,
    val messageSubscriptionRepository: MessageSubscriptionRepository
) : GraphQLResolver<Process> {

    fun processInstances(process: Process, perPage: Int, page: Int, stateIn: List<ProcessInstanceState>): ProcessInstanceConnection {
        return ProcessInstanceConnection(
                getItems = { processInstanceRepository.findByProcessDefinitionKeyAndStateIn(process.key, stateIn, PageRequest.of(page, perPage)).toList() },
                getCount = { processInstanceRepository.countByProcessDefinitionKeyAndStateIn(process.key, stateIn) }
        )
    }

    fun deployTime(process: Process, zoneId: String): String? {
        return process.deployTime.let { ResolverExtension.timestampToString(it, zoneId) }
    }

    fun timers(process: Process): List<Timer> {
        return timerRepository.findByProcessDefinitionKey(process.key)
    }

    fun messageSubscriptions(process: Process): List<MessageSubscription> {
        return messageSubscriptionRepository.findByProcessDefinitionKey(process.key)
    }

}
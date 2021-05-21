package io.zeebe.zeeqs.graphql.resolvers.connection

import graphql.kickstart.tools.GraphQLResolver
import io.zeebe.zeeqs.data.entity.MessageSubscription
import io.zeebe.zeeqs.data.entity.Timer
import io.zeebe.zeeqs.data.entity.Process
import io.zeebe.zeeqs.data.entity.ProcessInstanceState
import io.zeebe.zeeqs.data.repository.MessageSubscriptionRepository
import io.zeebe.zeeqs.data.repository.TimerRepository
import io.zeebe.zeeqs.data.repository.WorkflowInstanceRepository
import io.zeebe.zeeqs.graphql.resolvers.type.ResolverExtension
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component

@Component
class WorkflowResolver(
        val workflowInstanceRepository: WorkflowInstanceRepository,
        val timerRepository: TimerRepository,
        val messageSubscriptionRepository: MessageSubscriptionRepository
) : GraphQLResolver<Process> {

    fun workflowInstances(process: Process, perPage: Int, page: Int, stateIn: List<ProcessInstanceState>): WorkflowInstanceConnection {
        return WorkflowInstanceConnection(
                getItems = { workflowInstanceRepository.findByWorkflowKeyAndStateIn(process.key, stateIn, PageRequest.of(page, perPage)).toList() },
                getCount = { workflowInstanceRepository.countByWorkflowKeyAndStateIn(process.key, stateIn) }
        )
    }

    fun deployTime(process: Process, zoneId: String): String? {
        return process.deployTime.let { ResolverExtension.timestampToString(it, zoneId) }
    }

    fun timers(process: Process): List<Timer> {
        return timerRepository.findByWorkflowKey(process.key)
    }

    fun messageSubscriptions(process: Process): List<MessageSubscription> {
        return messageSubscriptionRepository.findByWorkflowKey(process.key)
    }

}
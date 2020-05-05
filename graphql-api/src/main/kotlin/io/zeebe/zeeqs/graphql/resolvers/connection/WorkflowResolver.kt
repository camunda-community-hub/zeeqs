package io.zeebe.zeeqs.graphql.resolvers.connection

import com.coxautodev.graphql.tools.GraphQLResolver
import io.zeebe.zeeqs.data.entity.MessageSubscription
import io.zeebe.zeeqs.data.entity.Timer
import io.zeebe.zeeqs.data.entity.Workflow
import io.zeebe.zeeqs.data.entity.WorkflowInstanceState
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
) : GraphQLResolver<Workflow> {

    fun workflowInstances(workflow: Workflow, limit: Int, page: Int, stateIn: List<WorkflowInstanceState>): WorkflowInstanceConnection {
        return WorkflowInstanceConnection(
                getItems = { workflowInstanceRepository.findByWorkflowKeyAndStateIn(workflow.key, stateIn, PageRequest.of(page, limit)).toList() },
                getCount = { workflowInstanceRepository.countByWorkflowKeyAndStateIn(workflow.key, stateIn) }
        )
    }

    fun deployTime(workflow: Workflow, zoneId: String): String? {
        return workflow.deployTime.let { ResolverExtension.timestampToString(it, zoneId) }
    }

    fun timers(workflow: Workflow): List<Timer> {
        return timerRepository.findByWorkflowKey(workflow.key)
    }

    fun messageSubscriptions(workflow: Workflow): List<MessageSubscription> {
        return messageSubscriptionRepository.findByWorkflowKey(workflow.key)
    }

}
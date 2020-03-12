package io.zeebe.zeeqs.graphql.resolvers.type

import com.coxautodev.graphql.tools.GraphQLResolver
import io.zeebe.zeeqs.data.entity.*
import io.zeebe.zeeqs.data.repository.MessageSubscriptionRepository
import io.zeebe.zeeqs.data.repository.TimerRepository
import io.zeebe.zeeqs.data.repository.WorkflowInstanceRepository
import org.springframework.stereotype.Component

@Component
class WorkflowResolver(
        val workflowInstanceRepository: WorkflowInstanceRepository,
        val timerRepository: TimerRepository,
        val messageSubscriptionRepository: MessageSubscriptionRepository
) : GraphQLResolver<Workflow> {

    fun workflowInstances(workflow: Workflow, stateIn: List<WorkflowInstanceState>): List<WorkflowInstance> {
        return workflowInstanceRepository.findByWorkflowKeyAndStateIn(workflow.key, stateIn)
    }

    fun timers(workflow: Workflow): List<Timer> {
        return timerRepository.findByWorkflowKey(workflow.key)
    }

    fun messageSubscriptions(workflow: Workflow): List<MessageSubscription> {
        return messageSubscriptionRepository.findByWorkflowKey(workflow.key)
    }

}
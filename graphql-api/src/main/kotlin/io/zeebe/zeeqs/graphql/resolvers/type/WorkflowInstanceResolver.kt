package io.zeebe.zeeqs.data.resolvers

import graphql.kickstart.tools.GraphQLResolver
import io.zeebe.zeeqs.data.entity.*
import io.zeebe.zeeqs.data.repository.*
import io.zeebe.zeeqs.graphql.resolvers.type.ResolverExtension.timestampToString
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class WorkflowInstanceResolver(
        val workflowInstanceRepository: WorkflowInstanceRepository,
        val variableRepository: VariableRepository,
        val workflowRepository: WorkflowRepository,
        val jobRepository: JobRepository,
        val incidentRepository: IncidentRepository,
        val elementInstanceRepository: ElementInstanceRepository,
        val timerRepository: TimerRepository,
        val messageSubscriptionRepository: MessageSubscriptionRepository
) : GraphQLResolver<WorkflowInstance> {

    fun startTime(workflowInstance: WorkflowInstance, zoneId: String): String? {
        return workflowInstance.startTime?.let { timestampToString(it, zoneId) }
    }

    fun endTime(workflowInstance: WorkflowInstance, zoneId: String): String? {
        return workflowInstance.endTime?.let { timestampToString(it, zoneId) }
    }

    fun variables(workflowInstance: WorkflowInstance): List<Variable> {
        return variableRepository.findByWorkflowInstanceKey(workflowInstance.key)
    }

    fun jobs(workflowInstance: WorkflowInstance, stateIn: List<JobState>, jobTypeIn: List<String>): List<Job> {
        return if (jobTypeIn.isEmpty()) {
            jobRepository.findByWorkflowInstanceKeyAndStateIn(workflowInstance.key, stateIn)
        } else {
            jobRepository.findByWorkflowInstanceKeyAndStateInAndJobTypeIn(
                    workflowInstanceKey = workflowInstance.key,
                    stateIn = stateIn,
                    jobTypeIn = jobTypeIn
            )
        }
    }

    fun workflow(workflowInstance: WorkflowInstance): Workflow? {
        return workflowRepository.findByIdOrNull(workflowInstance.workflowKey)
    }

    fun incidents(workflowInstance: WorkflowInstance, stateIn: List<IncidentState>): List<Incident> {
        return incidentRepository.findByWorkflowInstanceKeyAndStateIn(
                workflowInstanceKey = workflowInstance.key,
                stateIn = stateIn
        )
    }

    fun parentElementInstance(workflowInstance: WorkflowInstance): ElementInstance? {
        return workflowInstance.parentElementInstanceKey?.let { elementInstanceRepository.findByIdOrNull(it) }
    }

    fun childWorkflowInstances(workflowInstance: WorkflowInstance): List<WorkflowInstance> {
        return workflowInstanceRepository.findByParentWorkflowInstanceKey(workflowInstance.key)
    }

    fun elementInstances(workflowInstance: WorkflowInstance, stateIn: List<ElementInstanceState>): List<ElementInstance> {
        return elementInstanceRepository.findByWorkflowInstanceKeyAndStateIn(
                workflowInstanceKey = workflowInstance.key,
                stateIn = stateIn
        )
    }

    fun timers(workflowInstance: WorkflowInstance): List<Timer> {
        return timerRepository.findByWorkflowInstanceKey(workflowInstance.key)
    }

    fun messageSubscriptions(workflowInstance: WorkflowInstance): List<MessageSubscription> {
        return messageSubscriptionRepository.findByWorkflowInstanceKey(workflowInstance.key)
    }

}
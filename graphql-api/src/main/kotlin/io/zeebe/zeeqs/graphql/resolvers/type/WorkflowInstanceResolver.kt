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
    val processRepository: ProcessRepository,
    val jobRepository: JobRepository,
    val incidentRepository: IncidentRepository,
    val elementInstanceRepository: ElementInstanceRepository,
    val timerRepository: TimerRepository,
    val messageSubscriptionRepository: MessageSubscriptionRepository
) : GraphQLResolver<ProcessIntance> {

    fun startTime(processIntance: ProcessIntance, zoneId: String): String? {
        return processIntance.startTime?.let { timestampToString(it, zoneId) }
    }

    fun endTime(processIntance: ProcessIntance, zoneId: String): String? {
        return processIntance.endTime?.let { timestampToString(it, zoneId) }
    }

    fun variables(processIntance: ProcessIntance): List<Variable> {
        return variableRepository.findByWorkflowInstanceKey(processIntance.key)
    }

    fun jobs(processIntance: ProcessIntance, stateIn: List<JobState>, jobTypeIn: List<String>): List<Job> {
        return if (jobTypeIn.isEmpty()) {
            jobRepository.findByWorkflowInstanceKeyAndStateIn(processIntance.key, stateIn)
        } else {
            jobRepository.findByWorkflowInstanceKeyAndStateInAndJobTypeIn(
                    workflowInstanceKey = processIntance.key,
                    stateIn = stateIn,
                    jobTypeIn = jobTypeIn
            )
        }
    }

    fun workflow(processIntance: ProcessIntance): Process? {
        return processRepository.findByIdOrNull(processIntance.processDefinitionKey)
    }

    fun incidents(processIntance: ProcessIntance, stateIn: List<IncidentState>): List<Incident> {
        return incidentRepository.findByWorkflowInstanceKeyAndStateIn(
                workflowInstanceKey = processIntance.key,
                stateIn = stateIn
        )
    }

    fun parentElementInstance(processIntance: ProcessIntance): ElementInstance? {
        return processIntance.parentElementInstanceKey?.let { elementInstanceRepository.findByIdOrNull(it) }
    }

    fun childWorkflowInstances(processIntance: ProcessIntance): List<ProcessIntance> {
        return workflowInstanceRepository.findByParentWorkflowInstanceKey(processIntance.key)
    }

    fun elementInstances(processIntance: ProcessIntance, stateIn: List<ElementInstanceState>): List<ElementInstance> {
        return elementInstanceRepository.findByWorkflowInstanceKeyAndStateIn(
                workflowInstanceKey = processIntance.key,
                stateIn = stateIn
        )
    }

    fun timers(processIntance: ProcessIntance): List<Timer> {
        return timerRepository.findByWorkflowInstanceKey(processIntance.key)
    }

    fun messageSubscriptions(processIntance: ProcessIntance): List<MessageSubscription> {
        return messageSubscriptionRepository.findByWorkflowInstanceKey(processIntance.key)
    }

}
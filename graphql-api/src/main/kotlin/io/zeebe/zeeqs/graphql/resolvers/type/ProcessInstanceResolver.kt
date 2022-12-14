package io.zeebe.zeeqs.data.resolvers

import graphql.kickstart.tools.GraphQLResolver
import io.zeebe.zeeqs.data.entity.*
import io.zeebe.zeeqs.data.repository.*
import io.zeebe.zeeqs.data.service.VariableService
import io.zeebe.zeeqs.graphql.resolvers.connection.UserTaskConnection
import io.zeebe.zeeqs.graphql.resolvers.type.ResolverExtension.timestampToString
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class ProcessInstanceResolver(
        val processInstanceRepository: ProcessInstanceRepository,
        val variableRepository: VariableRepository,
        val processRepository: ProcessRepository,
        val jobRepository: JobRepository,
        val userTaskRepository: UserTaskRepository,
        val incidentRepository: IncidentRepository,
        val elementInstanceRepository: ElementInstanceRepository,
        val timerRepository: TimerRepository,
        val messageSubscriptionRepository: MessageSubscriptionRepository,
        val errorRepository: ErrorRepository
) : GraphQLResolver<ProcessInstance> {

    fun startTime(processInstance: ProcessInstance, zoneId: String): String? {
        return processInstance.startTime?.let { timestampToString(it, zoneId) }
    }

    fun endTime(processInstance: ProcessInstance, zoneId: String): String? {
        return processInstance.endTime?.let { timestampToString(it, zoneId) }
    }

    fun variables(processInstance: ProcessInstance, globalOnly: Boolean): List<Variable> {
        return if (globalOnly) {
            variableRepository.findByScopeKey(scopeKey = processInstance.key)
        } else {
            variableRepository.findByProcessInstanceKey(processInstanceKey = processInstance.key)
        }
    }

    fun jobs(processInstance: ProcessInstance, stateIn: List<JobState>, jobTypeIn: List<String>): List<Job> {
        return if (jobTypeIn.isEmpty()) {
            jobRepository.findByProcessInstanceKeyAndStateIn(processInstance.key, stateIn)
        } else {
            jobRepository.findByProcessInstanceKeyAndStateInAndJobTypeIn(
                    processInstanceKey = processInstance.key,
                    stateIn = stateIn,
                    jobTypeIn = jobTypeIn
            )
        }
    }

    fun userTasks(processInstance: ProcessInstance, perPage: Int, page: Int, stateIn: List<UserTaskState>): UserTaskConnection {
        return UserTaskConnection(
                getItems = {
                    userTaskRepository.findByProcessInstanceKeyAndStateIn(
                            processInstanceKey = processInstance.key,
                            stateIn = stateIn,
                            pageable = PageRequest.of(page, perPage))
                },
                getCount = {
                    userTaskRepository.countByProcessInstanceKeyAndStateIn(
                            processInstanceKey = processInstance.key,
                            stateIn = stateIn
                    )
                }
        )
    }

    fun process(processInstance: ProcessInstance): Process? {
        return processRepository.findByIdOrNull(processInstance.processDefinitionKey)
    }

    fun incidents(processInstance: ProcessInstance, stateIn: List<IncidentState>): List<Incident> {
        return incidentRepository.findByProcessInstanceKeyAndStateIn(
                processInstanceKey = processInstance.key,
                stateIn = stateIn
        )
    }

    fun parentElementInstance(processInstance: ProcessInstance): ElementInstance? {
        return processInstance.parentElementInstanceKey?.let { elementInstanceRepository.findByIdOrNull(it) }
    }

    fun childProcessInstances(processInstance: ProcessInstance): List<ProcessInstance> {
        return processInstanceRepository.findByParentProcessInstanceKey(processInstance.key)
    }

    fun elementInstances(processInstance: ProcessInstance, stateIn: List<ElementInstanceState>): List<ElementInstance> {
        return elementInstanceRepository.findByProcessInstanceKeyAndStateIn(
                processInstanceKey = processInstance.key,
                stateIn = stateIn
        )
    }

    fun timers(processInstance: ProcessInstance): List<Timer> {
        return timerRepository.findByProcessInstanceKey(processInstance.key)
    }

    fun messageSubscriptions(processInstance: ProcessInstance): List<MessageSubscription> {
        return messageSubscriptionRepository.findByProcessInstanceKey(processInstance.key)
    }

    fun error(processInstance: ProcessInstance): Error? {
        return errorRepository.findByProcessInstanceKey(processInstance.key)
    }

}
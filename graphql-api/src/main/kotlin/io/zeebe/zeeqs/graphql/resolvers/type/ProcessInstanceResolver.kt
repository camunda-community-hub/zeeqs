package io.zeebe.zeeqs.graphql.resolvers.type

import io.zeebe.zeeqs.data.entity.*
import io.zeebe.zeeqs.data.repository.*
import io.zeebe.zeeqs.graphql.resolvers.connection.DecisionEvaluationConnection
import io.zeebe.zeeqs.graphql.resolvers.connection.UserTaskConnection
import io.zeebe.zeeqs.graphql.resolvers.type.ResolverExtension.timestampToString
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
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
    val errorRepository: ErrorRepository,
    private val decisionEvaluationRepository: DecisionEvaluationRepository
) {

    @SchemaMapping(typeName = "ProcessInstance", field = "startTime")
    fun startTime(
        processInstance: ProcessInstance,
        @Argument zoneId: String
    ): String? {
        return processInstance.startTime?.let { timestampToString(it, zoneId) }
    }

    @SchemaMapping(typeName = "ProcessInstance", field = "endTime")
    fun endTime(
        processInstance: ProcessInstance,
        @Argument zoneId: String
    ): String? {
        return processInstance.endTime?.let { timestampToString(it, zoneId) }
    }

    @SchemaMapping(typeName = "ProcessInstance", field = "variables")
    fun variables(
        processInstance: ProcessInstance,
        @Argument globalOnly: Boolean
    ): List<Variable> {
        return if (globalOnly) {
            variableRepository.findByScopeKey(scopeKey = processInstance.key)
        } else {
            variableRepository.findByProcessInstanceKey(processInstanceKey = processInstance.key)
        }
    }

    @SchemaMapping(typeName = "ProcessInstance", field = "jobs")
    fun jobs(
        processInstance: ProcessInstance,
        @Argument stateIn: List<JobState>,
        @Argument jobTypeIn: List<String>
    ): List<Job> {
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

    @SchemaMapping(typeName = "ProcessInstance", field = "userTasks")
    fun userTasks(
        processInstance: ProcessInstance,
        @Argument perPage: Int,
        @Argument page: Int,
        @Argument stateIn: List<UserTaskState>
    ): UserTaskConnection {
        return UserTaskConnection(
            getItems = {
                userTaskRepository.findByProcessInstanceKeyAndStateIn(
                    processInstanceKey = processInstance.key,
                    stateIn = stateIn,
                    pageable = PageRequest.of(page, perPage)
                )
            },
            getCount = {
                userTaskRepository.countByProcessInstanceKeyAndStateIn(
                    processInstanceKey = processInstance.key,
                    stateIn = stateIn
                )
            }
        )
    }

    @SchemaMapping(typeName = "ProcessInstance", field = "process")
    fun process(processInstance: ProcessInstance): Process? {
        return processRepository.findByIdOrNull(processInstance.processDefinitionKey)
    }

    @SchemaMapping(typeName = "ProcessInstance", field = "incidents")
    fun incidents(
        processInstance: ProcessInstance,
        @Argument stateIn: List<IncidentState>
    ): List<Incident> {
        return incidentRepository.findByProcessInstanceKeyAndStateIn(
            processInstanceKey = processInstance.key,
            stateIn = stateIn
        )
    }

    @SchemaMapping(typeName = "ProcessInstance", field = "parentElementInstance")
    fun parentElementInstance(processInstance: ProcessInstance): ElementInstance? {
        return processInstance.parentElementInstanceKey?.let {
            elementInstanceRepository.findByIdOrNull(
                it
            )
        }
    }

    @SchemaMapping(typeName = "ProcessInstance", field = "childProcessInstances")
    fun childProcessInstances(processInstance: ProcessInstance): List<ProcessInstance> {
        return processInstanceRepository.findByParentProcessInstanceKey(processInstance.key)
    }

    @SchemaMapping(typeName = "ProcessInstance", field = "elementInstances")
    fun elementInstances(
        processInstance: ProcessInstance,
        @Argument stateIn: List<ElementInstanceState>
    ): List<ElementInstance> {
        return elementInstanceRepository.findByProcessInstanceKeyAndStateIn(
            processInstanceKey = processInstance.key,
            stateIn = stateIn
        )
    }

    @SchemaMapping(typeName = "ProcessInstance", field = "timers")
    fun timers(processInstance: ProcessInstance): List<Timer> {
        // all timers of the process instance must have an element instance key
        // - timers for process timer start events have a process instance key after triggered
        return timerRepository.findByProcessInstanceKeyAndElementInstanceKeyIsNotNull(
            processInstance.key
        )
    }

    @SchemaMapping(typeName = "ProcessInstance", field = "messageSubscriptions")
    fun messageSubscriptions(processInstance: ProcessInstance): List<MessageSubscription> {
        return messageSubscriptionRepository.findByProcessInstanceKey(processInstance.key)
    }

    @SchemaMapping(typeName = "ProcessInstance", field = "error")
    fun error(processInstance: ProcessInstance): Error? {
        return errorRepository.findByProcessInstanceKey(processInstance.key)
    }

    @SchemaMapping(typeName = "ProcessInstance", field = "decisionEvaluations")
    fun decisionEvaluations(
        processInstance: ProcessInstance,
        @Argument perPage: Int,
        @Argument page: Int,
        @Argument stateIn: List<DecisionEvaluationState>
    ): DecisionEvaluationConnection {
        return DecisionEvaluationConnection(
            getItems = {
                decisionEvaluationRepository.findAllByProcessInstanceKeyAndStateIn(
                    processInstanceKey = processInstance.key,
                    stateIn = stateIn,
                    pageable = PageRequest.of(page, perPage)
                )
            },
            getCount = {
                decisionEvaluationRepository.countByProcessInstanceKeyAndStateIn(
                    processInstanceKey = processInstance.key,
                    stateIn = stateIn
                )
            }
        )
    }
}
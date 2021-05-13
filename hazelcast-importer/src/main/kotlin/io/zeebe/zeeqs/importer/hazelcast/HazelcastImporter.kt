package io.zeebe.zeeqs.importer.hazelcast

import com.hazelcast.client.HazelcastClient
import com.hazelcast.client.config.ClientConfig
import io.zeebe.exporter.proto.Schema
import io.zeebe.hazelcast.connect.java.ZeebeHazelcast
import io.zeebe.zeeqs.data.entity.*
import io.zeebe.zeeqs.data.repository.*
import org.springframework.stereotype.Component
import java.time.Duration


@Component
class HazelcastImporter(
        val hazelcastConfigRepository: HazelcastConfigRepository,
        val workflowRepository: WorkflowRepository,
        val workflowInstanceRepository: WorkflowInstanceRepository,
        val elementInstanceRepository: ElementInstanceRepository,
        val elementInstanceStateTransitionRepository: ElementInstanceStateTransitionRepository,
        val variableRepository: VariableRepository,
        val variableUpdateRepository: VariableUpdateRepository,
        val jobRepository: JobRepository,
        val incidentRepository: IncidentRepository,
        val timerRepository: TimerRepository,
        val messageRepository: MessageRepository,
        val messageSubscriptionRepository: MessageSubscriptionRepository,
        val messageCorrelationRepository: MessageCorrelationRepository,
        val errorRepository: ErrorRepository
        ) {

    var zeebeHazelcast: ZeebeHazelcast? = null

    fun start(hazelcastProperties: HazelcastProperties) {

        val hazelcastConnection = hazelcastProperties.connection
        val hazelcastConnectionTimeout = Duration.parse(hazelcastProperties.connectionTimeout)
        val hazelcastRingbuffer = hazelcastProperties.ringbuffer
        val hazelcastConnectionInitialBackoff = Duration.parse(hazelcastProperties.connectionInitialBackoff)
        val hazelcastConnectionBackoffMultiplier = hazelcastProperties.connectionBackoffMultiplier
        val hazelcastConnectionMaxBackoff = Duration.parse(hazelcastProperties.connectionMaxBackoff)

        val hazelcastConfig = hazelcastConfigRepository.findById(hazelcastConnection)
                .orElse(HazelcastConfig(
                        id = hazelcastConnection,
                        sequence = -1))

        val updateSequence: ((Long) -> Unit) = {
            hazelcastConfig.sequence = it
            hazelcastConfigRepository.save(hazelcastConfig)
        }

        val clientConfig = ClientConfig()
        val networkConfig = clientConfig.networkConfig
        networkConfig.addresses = listOf(hazelcastConnection)

        val connectionRetryConfig = clientConfig.connectionStrategyConfig.connectionRetryConfig
        connectionRetryConfig.clusterConnectTimeoutMillis = hazelcastConnectionTimeout.toMillis()
        // These retry configs can be user-configured in application.yml
        connectionRetryConfig.initialBackoffMillis = hazelcastConnectionInitialBackoff.toMillis().toInt()
        connectionRetryConfig.multiplier = hazelcastConnectionBackoffMultiplier
        connectionRetryConfig.maxBackoffMillis = hazelcastConnectionMaxBackoff.toMillis().toInt()

        val hazelcast = HazelcastClient.newHazelcastClient(clientConfig)

        val builder = ZeebeHazelcast.newBuilder(hazelcast).name(hazelcastRingbuffer)
                .addDeploymentListener { it.takeIf { it.metadata.key > 0 }?.let(this::importDeploymentRecord) }
                .addProcessInstanceListener { it.takeIf { it.metadata.key > 0 }?.let(this::importWorkflowInstanceRecord) }
                .addVariableListener { it.takeIf { it.metadata.key > 0 }?.let(this::importVariableRecord) }
                .addJobListener { it.takeIf { it.metadata.key > 0 }?.let(this::importJobRecord) }
                .addIncidentListener { it.takeIf { it.metadata.key > 0 }?.let(this::importIncidentRecord) }
                .addTimerListener { it.takeIf { it.metadata.key > 0 }?.let(this::importTimerRecord) }
                .addMessageListener { it.takeIf { it.metadata.key > 0 }?.let(this::importMessageRecord) }
                .addMessageSubscriptionListener(this::importMessageSubscriptionRecord)
                .addMessageStartEventSubscriptionListener(this::importMessageStartEventSubscriptionRecord)
                .addProcessMessageSubscriptionListener { it.takeIf { it.metadata.key > 0 }?.let(this::importWorkflowInstanceSubscriptionRecord) }
                .addErrorListener(this::importError)
                .postProcessListener(updateSequence)

        if (hazelcastConfig.sequence >= 0) {
            builder.readFrom(hazelcastConfig.sequence)
        } else {
            builder.readFromHead()
        }

        zeebeHazelcast = builder.build()
    }

    fun stop() {
        zeebeHazelcast?.close()
    }

    private fun importDeploymentRecord(record: Schema.DeploymentRecord) {
        for (workflow in record.processMetadataList) {
            val resource = record.resourcesList.first { it.resourceName == workflow.resourceName }

            importWorkflow(record, workflow, resource)
        }
    }

    private fun importWorkflow(deployment: Schema.DeploymentRecord,
                               workflow: Schema.DeploymentRecord.ProcessMetadata,
                               resource: Schema.DeploymentRecord.Resource) {
        val entity = workflowRepository
                .findById(workflow.processDefinitionKey)
                .orElse(createWorkflow(deployment, workflow, resource))

        workflowRepository.save(entity)
    }

    private fun createWorkflow(deployment: Schema.DeploymentRecord,
                               workflow: Schema.DeploymentRecord.ProcessMetadata,
                               resource: Schema.DeploymentRecord.Resource): Workflow {
        return Workflow(
                key = workflow.processDefinitionKey,
                bpmnProcessId = workflow.bpmnProcessId,
                version = workflow.version,
                bpmnXML = resource.resource.toStringUtf8(),
                deployTime = deployment.metadata.timestamp
        )
    }

    private fun importWorkflowInstanceRecord(record: Schema.ProcessInstanceRecord) {
        if (record.processInstanceKey == record.metadata.key) {
            importWorkflowInstance(record)
        }

        importElementInstance(record)
        importElementInstanceStateTransition(record)
    }

    private fun importWorkflowInstance(record: Schema.ProcessInstanceRecord) {
        val entity = workflowInstanceRepository
                .findById(record.processInstanceKey)
                .orElse(createWorkflowInstance(record))

        when (record.metadata.intent) {
            "ELEMENT_ACTIVATED" -> {
                entity.startTime = record.metadata.timestamp
                entity.state = WorkflowInstanceState.ACTIVATED
            }
            "ELEMENT_COMPLETED" -> {
                entity.endTime = record.metadata.timestamp
                entity.state = WorkflowInstanceState.COMPLETED
            }
            "ELEMENT_TERMINATED" -> {
                entity.endTime = record.metadata.timestamp
                entity.state = WorkflowInstanceState.TERMINATED
            }
        }

        workflowInstanceRepository.save(entity)
    }

    private fun createWorkflowInstance(record: Schema.ProcessInstanceRecord): WorkflowInstance {
        return WorkflowInstance(
                key = record.processInstanceKey,
                bpmnProcessId = record.bpmnProcessId,
                version = record.version,
                workflowKey = record.processDefinitionKey,
                parentWorkflowInstanceKey = record.parentProcessInstanceKey.takeIf { it > 0 },
                parentElementInstanceKey = record.parentElementInstanceKey.takeIf { it > 0 }
        )
    }

    private fun importElementInstance(record: Schema.ProcessInstanceRecord) {
        val entity = elementInstanceRepository
                .findById(record.metadata.key)
                .orElse(createElementInstance(record))

        entity.state = getElementInstanceState(record)

        when (record.metadata.intent) {
            "ELEMENT_ACTIVATING" -> {
                entity.startTime = record.metadata.timestamp
            }
            "ELEMENT_COMPLETED", "ELEMENT_TERMINATED" -> {
                entity.endTime = record.metadata.timestamp
            }
            "SEQUENCE_FLOW_TAKEN" -> {
                entity.startTime = record.metadata.timestamp
                entity.endTime = record.metadata.timestamp
            }
        }

        elementInstanceRepository.save(entity)
    }

    private fun createElementInstance(record: Schema.ProcessInstanceRecord): ElementInstance {
        val bpmnElementType = when (record.bpmnElementType) {

            "BOUNDARY_EVENT" -> BpmnElementType.BOUNDARY_EVENT
            "CALL_ACTIVITY" -> BpmnElementType.CALL_ACTIVITY
            "END_EVENT" -> BpmnElementType.END_EVENT
            "EVENT_BASED_GATEWAY" -> BpmnElementType.EVENT_BASED_GATEWAY
            "EXCLUSIVE_GATEWAY" -> BpmnElementType.EXCLUSIVE_GATEWAY
            "INTERMEDIATE_CATCH_EVENT" -> BpmnElementType.INTERMEDIATE_CATCH_EVENT
            "PARALLEL_GATEWAY" -> BpmnElementType.PARALLEL_GATEWAY
            "PROCESS" -> BpmnElementType.PROCESS
            "RECEIVE_TASK" -> BpmnElementType.RECEIVE_TASK
            "SEQUENCE_FLOW" -> BpmnElementType.SEQUENCE_FLOW
            "SERVICE_TASK" -> BpmnElementType.SERVICE_TASK
            "START_EVENT" -> BpmnElementType.START_EVENT
            "SUB_PROCESS" -> BpmnElementType.SUB_PROCESS
            "USER_TASK" -> BpmnElementType.USER_TASK
            else -> BpmnElementType.UNSPECIFIED
        }

        return ElementInstance(
                key = record.metadata.key,
                elementId = record.elementId,
                bpmnElementType = bpmnElementType,
                workflowInstanceKey = record.processInstanceKey,
                workflowKey = record.processDefinitionKey,
                scopeKey = record.flowScopeKey.takeIf { it > 0 }
        )
    }

    private fun getElementInstanceState(record: Schema.ProcessInstanceRecord): ElementInstanceState {
        return when (record.metadata.intent) {
            "ELEMENT_ACTIVATING" -> ElementInstanceState.ACTIVATING
            "ELEMENT_ACTIVATED" -> ElementInstanceState.ACTIVATED
            "ELEMENT_COMPLETING" -> ElementInstanceState.COMPLETING
            "ELEMENT_COMPLETED" -> ElementInstanceState.COMPLETED
            "ELEMENT_TERMINATING" -> ElementInstanceState.TERMINATING
            "ELEMENT_TERMINATED" -> ElementInstanceState.TERMINATED
            "EVENT_OCCURRED" -> ElementInstanceState.EVENT_OCCURRED
            "SEQUENCE_FLOW_TAKEN" -> ElementInstanceState.TAKEN
            else -> ElementInstanceState.ACTIVATING
        }
    }

    private fun importElementInstanceStateTransition(record: Schema.ProcessInstanceRecord) {

        val state = getElementInstanceState(record)

        val entity = elementInstanceStateTransitionRepository
                .findById(record.metadata.position)
                .orElse(ElementInstanceStateTransition(
                        position = record.metadata.position,
                        elementInstanceKey = record.metadata.key,
                        timestamp = record.metadata.timestamp,
                        state = state
                ))

        elementInstanceStateTransitionRepository.save(entity)
    }

    private fun importVariableRecord(record: Schema.VariableRecord) {
        importVariable(record)
        importVariableUpdate(record)
    }

    private fun importVariable(record: Schema.VariableRecord) {

        val entity = variableRepository
                .findById(record.metadata.key)
                .orElse(createVariable(record))

        entity.value = record.value
        entity.timestamp = record.metadata.timestamp

        variableRepository.save(entity)
    }

    private fun createVariable(record: Schema.VariableRecord): Variable {
        return Variable(
                key = record.metadata.key,
                name = record.name,
                value = record.value,
                workflowInstanceKey = record.processInstanceKey,
                scopeKey = record.scopeKey,
                timestamp = record.metadata.timestamp
        )
    }

    private fun importVariableUpdate(record: Schema.VariableRecord) {

        val entity = variableUpdateRepository
                .findById(record.metadata.position)
                .orElse(VariableUpdate(
                        position = record.metadata.position,
                        variableKey = record.metadata.key,
                        name = record.name,
                        value = record.value,
                        workflowInstanceKey = record.processInstanceKey,
                        scopeKey = record.scopeKey,
                        timestamp = record.metadata.timestamp
                ))

        variableUpdateRepository.save(entity)
    }

    private fun importJobRecord(record: Schema.JobRecord) {
        val entity = jobRepository
                .findById(record.metadata.key)
                .orElse(createJob(record))

        when (record.metadata.intent) {
            "CREATED" -> {
                entity.state = JobState.ACTIVATABLE
                entity.startTime = record.metadata.timestamp
            }
            "TIMED_OUT", "RETRIES_UPDATED" -> entity.state = JobState.ACTIVATABLE
            "ACTIVATED" -> entity.state = JobState.ACTIVATED
            "FAILED" -> entity.state = JobState.FAILED
            "COMPLETED" -> {
                entity.state = JobState.COMPLETED
                entity.endTime = record.metadata.timestamp
            }
            "CANCELED" -> {
                entity.state = JobState.CANCELED
                entity.endTime = record.metadata.timestamp
            }
            "ERROR_THROWN" -> {
                entity.state = JobState.ERROR_THROWN
                entity.endTime = record.metadata.timestamp
            }
        }

        entity.worker = record.worker.ifEmpty { null }
        entity.retries = record.retries
        entity.timestamp = record.metadata.timestamp

        jobRepository.save(entity)
    }

    private fun createJob(record: Schema.JobRecord): Job {
        return Job(
                key = record.metadata.key,
                jobType = record.type,
                workflowInstanceKey = record.processInstanceKey,
                elementInstanceKey = record.elementInstanceKey
        )
    }

    private fun importIncidentRecord(record: Schema.IncidentRecord) {
        val entity = incidentRepository
                .findById(record.metadata.key)
                .orElse(createIncident(record))

        when (record.metadata.intent) {
            "CREATED" -> {
                entity.state = IncidentState.CREATED
                entity.creationTime = record.metadata.timestamp
            }
            "RESOLVED" -> {
                entity.state = IncidentState.RESOLVED
                entity.resolveTime = record.metadata.timestamp
            }
        }

        incidentRepository.save(entity)
    }

    private fun createIncident(record: Schema.IncidentRecord): Incident {
        return Incident(
                key = record.metadata.key,
                errorType = record.errorType,
                errorMessage = record.errorMessage,
                workflowInstanceKey = record.processInstanceKey,
                elementInstanceKey = record.elementInstanceKey,
                jobKey = record.jobKey.takeIf { it > 0 }
        )
    }

    private fun importTimerRecord(record: Schema.TimerRecord) {
        val entity = timerRepository
                .findById(record.metadata.key)
                .orElse(createTimer(record))

        when (record.metadata.intent) {
            "CREATED" -> {
                entity.state = TimerState.CREATED
                entity.startTime = record.metadata.timestamp
            }
            "TRIGGERED" -> {
                entity.state = TimerState.TRIGGERED
                entity.endTime = record.metadata.timestamp
            }
            "CANCELED" -> {
                entity.state = TimerState.CANCELED
                entity.endTime = record.metadata.timestamp
            }
        }

        entity.repetitions = record.repetitions

        timerRepository.save(entity)
    }

    private fun createTimer(record: Schema.TimerRecord): Timer {
        return Timer(
                key = record.metadata.key,
                dueDate = record.dueDate,
                repetitions = record.repetitions,
                workflowKey = record.processDefinitionKey.takeIf { it > 0 },
                workflowInstanceKey = record.processInstanceKey.takeIf { it > 0 },
                elementInstanceKey = record.elementInstanceKey.takeIf { it > 0 }
        );
    }

    private fun importMessageRecord(record: Schema.MessageRecord) {
        val entity = messageRepository
                .findById(record.metadata.key)
                .orElse(createMessage(record))

        when (record.metadata.intent) {
            "PUBLISHED" -> entity.state = MessageState.PUBLISHED
            "DELETED" -> entity.state = MessageState.DELETED
        }

        entity.timestamp = record.metadata.timestamp

        messageRepository.save(entity)
    }

    private fun createMessage(record: Schema.MessageRecord): Message {
        return Message(
                key = record.metadata.key,
                name = record.name,
                correlationKey = record.correlationKey.takeIf { it.isNotEmpty() },
                messageId = record.messageId.takeIf { it.isNotEmpty() },
                timeToLive = record.timeToLive
        );
    }

    private fun importMessageSubscriptionRecord(record: Schema.MessageSubscriptionRecord) {
        val entity = messageSubscriptionRepository
                .findByElementInstanceKeyAndMessageName(record.elementInstanceKey, record.messageName)
                ?: (createMessageSubscription(record))

        when (record.metadata.intent) {
            "OPENED" -> entity.state = MessageSubscriptionState.OPENED
            "CORRELATED" -> entity.state = MessageSubscriptionState.CORRELATED
            "CLOSED" -> entity.state = MessageSubscriptionState.CLOSED
        }

        entity.timestamp = record.metadata.timestamp

        messageSubscriptionRepository.save(entity)
    }

    private fun createMessageSubscription(record: Schema.MessageSubscriptionRecord): MessageSubscription {
        // TODO (saig0): message subscription doesn't have a key - https://github.com/zeebe-io/zeebe/issues/2805
        val key = record.metadata.position
        return MessageSubscription(
                key = key,
                messageName = record.messageName,
                messageCorrelationKey = record.correlationKey,
                workflowInstanceKey = record.processInstanceKey,
                elementInstanceKey = record.elementInstanceKey,
                elementId = null,
                workflowKey = null
        );
    }

    private fun importMessageStartEventSubscriptionRecord(record: Schema.MessageStartEventSubscriptionRecord) {
        val entity = messageSubscriptionRepository
                .findByWorkflowKeyAndMessageName(record.processDefinitionKey, record.messageName)
                ?: (createMessageSubscription(record))

        when (record.metadata.intent) {
            "OPENED" -> entity.state = MessageSubscriptionState.OPENED
            "CLOSED" -> entity.state = MessageSubscriptionState.CLOSED
        }

        entity.timestamp = record.metadata.timestamp

        messageSubscriptionRepository.save(entity)
    }

    private fun createMessageSubscription(record: Schema.MessageStartEventSubscriptionRecord): MessageSubscription {
        // TODO (saig0): message subscription doesn't have a key - https://github.com/zeebe-io/zeebe/issues/2805
        val key = record.metadata.position
        return MessageSubscription(
                key = key,
                messageName = record.messageName,
                workflowKey = record.processDefinitionKey,
                elementId = record.startEventId,
                elementInstanceKey = null,
                workflowInstanceKey = null,
                messageCorrelationKey = null
        );
    }

    private fun importWorkflowInstanceSubscriptionRecord(record: Schema.ProcessMessageSubscriptionRecord) {
        when (record.metadata.intent) {
            "CORRELATED" -> importMessageCorrelation(record)
        }
    }

    private fun importMessageCorrelation(record: Schema.ProcessMessageSubscriptionRecord) {

        val entity = messageCorrelationRepository
                .findById(record.metadata.position)
                .orElse(
                        MessageCorrelation(
                                position = record.metadata.position,
                                messageKey = record.messageKey,
                                messageName = record.messageName,
                                elementInstanceKey = record.elementInstanceKey,
                                timestamp = record.metadata.timestamp
                        ))

        messageCorrelationRepository.save(entity)
    }

    private fun importError(record: Schema.ErrorRecord) {

        val entity = errorRepository.findById(record.metadata.position)
                .orElse(Error(
                    position = record.metadata.position,
                      errorEventPosition = record.errorEventPosition,
                        exceptionMessage = record.exceptionMessage,
                        stacktrace = record.stacktrace,
                        workflowInstanceKey = record.processInstanceKey.takeIf { it > 0 }
                ))

        errorRepository.save(entity)
    }

}

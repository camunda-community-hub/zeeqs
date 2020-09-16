package io.zeebe.zeeqs.importer.protobuf

import io.zeebe.exporter.proto.Schema
import io.zeebe.zeeqs.data.entity.*
import io.zeebe.zeeqs.data.repository.*
import org.springframework.stereotype.Component
import java.time.Duration
import io.zeebe.exporter.source.ProtobufSourceConnector
import io.zeebe.exporter.source.ProtobufSource

@Component
class ProtobufImporter(
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
        val messageCorrelationRepository: MessageCorrelationRepository) : ProtobufSourceConnector { 

    override fun connectTo(source: ProtobufSource) {
      source.addDeploymentListener { it.takeIf { it.metadata.key > 0 }?.let(this::importDeploymentRecord) }
      source.addWorkflowInstanceListener { it.takeIf { it.metadata.key > 0 }?.let(this::importWorkflowInstanceRecord) }
      source.addVariableListener { it.takeIf { it.metadata.key > 0 }?.let(this::importVariableRecord) }
      source.addJobListener { it.takeIf { it.metadata.key > 0 }?.let(this::importJobRecord) }
      source.addIncidentListener { it.takeIf { it.metadata.key > 0 }?.let(this::importIncidentRecord) }
      source.addTimerListener { it.takeIf { it.metadata.key > 0 }?.let(this::importTimerRecord) }
      source.addMessageListener { it.takeIf { it.metadata.key > 0 }?.let(this::importMessageRecord) }
      source.addMessageSubscriptionListener(this::importMessageSubscriptionRecord)
      source.addMessageStartEventSubscriptionListener(this::importMessageStartEventSubscriptionRecord)
      source.addWorkflowInstanceSubscriptionListener { it.takeIf { it.metadata.key > 0 }?.let(this::importWorkflowInstanceSubscriptionRecord) }
    }

    private fun importDeploymentRecord(record: Schema.DeploymentRecord) {
        for (workflow in record.deployedWorkflowsList) {
            val resource = record.resourcesList.first { it.resourceName == workflow.resourceName }

            importWorkflow(record, workflow, resource)
        }
    }

    private fun importWorkflow(deployment: Schema.DeploymentRecord,
                               workflow: Schema.DeploymentRecord.Workflow,
                               resource: Schema.DeploymentRecord.Resource) {
        val entity = workflowRepository
                .findById(workflow.workflowKey)
                .orElse(createWorkflow(deployment, workflow, resource))

        workflowRepository.save(entity)
    }

    public fun createWorkflow(deployment: Schema.DeploymentRecord,
                               workflow: Schema.DeploymentRecord.Workflow,
                               resource: Schema.DeploymentRecord.Resource): Workflow {
        return Workflow(
                key = workflow.workflowKey,
                bpmnProcessId = workflow.bpmnProcessId,
                version = workflow.version,
                bpmnXML = resource.resource.toStringUtf8(),
                deployTime = deployment.metadata.timestamp
        )
    }

    private fun importWorkflowInstanceRecord(record: Schema.WorkflowInstanceRecord) {
        if (record.workflowInstanceKey == record.metadata.key) {
            importWorkflowInstance(record)
        }

        importElementInstance(record)
        importElementInstanceStateTransition(record)
    }

    private fun importWorkflowInstance(record: Schema.WorkflowInstanceRecord) {
        val entity = workflowInstanceRepository
                .findById(record.workflowInstanceKey)
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

    public fun createWorkflowInstance(record: Schema.WorkflowInstanceRecord): WorkflowInstance {
        return WorkflowInstance(
                key = record.workflowInstanceKey,
                bpmnProcessId = record.bpmnProcessId,
                version = record.version,
                workflowKey = record.workflowKey,
                parentWorkflowInstanceKey = record.parentWorkflowInstanceKey.takeIf { it > 0 },
                parentElementInstanceKey = record.parentElementInstanceKey.takeIf { it > 0 }
        )
    }

    private fun importElementInstance(record: Schema.WorkflowInstanceRecord) {
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

    public fun createElementInstance(record: Schema.WorkflowInstanceRecord): ElementInstance {

        val bpmnElementType = when (record.bpmnElementType) {
            Schema.WorkflowInstanceRecord.BpmnElementType.BOUNDARY_EVENT -> BpmnElementType.BOUNDARY_EVENT
            Schema.WorkflowInstanceRecord.BpmnElementType.CALL_ACTIVITY -> BpmnElementType.CALL_ACTIVITY
            Schema.WorkflowInstanceRecord.BpmnElementType.END_EVENT -> BpmnElementType.END_EVENT
            Schema.WorkflowInstanceRecord.BpmnElementType.EVENT_BASED_GATEWAY -> BpmnElementType.EVENT_BASED_GATEWAY
            Schema.WorkflowInstanceRecord.BpmnElementType.EXCLUSIVE_GATEWAY -> BpmnElementType.EXCLUSIVE_GATEWAY
            Schema.WorkflowInstanceRecord.BpmnElementType.INTERMEDIATE_CATCH_EVENT -> BpmnElementType.INTERMEDIATE_CATCH_EVENT
            Schema.WorkflowInstanceRecord.BpmnElementType.MULTI_INSTANCE_BODY -> BpmnElementType.MULTI_INSTANCE_BODY
            Schema.WorkflowInstanceRecord.BpmnElementType.PARALLEL_GATEWAY -> BpmnElementType.PARALLEL_GATEWAY
            Schema.WorkflowInstanceRecord.BpmnElementType.PROCESS -> BpmnElementType.PROCESS
            Schema.WorkflowInstanceRecord.BpmnElementType.RECEIVE_TASK -> BpmnElementType.RECEIVE_TASK
            Schema.WorkflowInstanceRecord.BpmnElementType.SEQUENCE_FLOW -> BpmnElementType.SEQUENCE_FLOW
            Schema.WorkflowInstanceRecord.BpmnElementType.SERVICE_TASK -> BpmnElementType.SERVICE_TASK
            Schema.WorkflowInstanceRecord.BpmnElementType.START_EVENT -> BpmnElementType.START_EVENT
            Schema.WorkflowInstanceRecord.BpmnElementType.SUB_PROCESS -> BpmnElementType.SUB_PROCESS
            else -> BpmnElementType.UNSPECIFIED
        }

        return ElementInstance(
                key = record.metadata.key,
                elementId = record.elementId,
                bpmnElementType = bpmnElementType,
                workflowInstanceKey = record.workflowInstanceKey,
                workflowKey = record.workflowKey,
                scopeKey = record.flowScopeKey.takeIf { it > 0 }
        )
    }

    public fun getElementInstanceState(record: Schema.WorkflowInstanceRecord): ElementInstanceState {
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

    private fun importElementInstanceStateTransition(record: Schema.WorkflowInstanceRecord) {

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

    public fun createVariable(record: Schema.VariableRecord): Variable {
        return Variable(
                key = record.metadata.key,
                name = record.name,
                value = record.value,
                workflowInstanceKey = record.workflowInstanceKey,
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
                        workflowInstanceKey = record.workflowInstanceKey,
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

    public fun createJob(record: Schema.JobRecord): Job {
        return Job(
                key = record.metadata.key,
                jobType = record.type,
                workflowInstanceKey = record.workflowInstanceKey,
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

    public fun createIncident(record: Schema.IncidentRecord): Incident {
        return Incident(
                key = record.metadata.key,
                errorType = record.errorType,
                errorMessage = record.errorMessage,
                workflowInstanceKey = record.workflowInstanceKey,
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

    public fun createTimer(record: Schema.TimerRecord): Timer {
        return Timer(
                key = record.metadata.key,
                dueDate = record.dueDate,
                repetitions = record.repetitions,
                workflowKey = record.workflowKey.takeIf { it > 0 },
                workflowInstanceKey = record.workflowInstanceKey.takeIf { it > 0 },
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

    public fun createMessage(record: Schema.MessageRecord): Message {
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

    public fun createMessageSubscription(record: Schema.MessageSubscriptionRecord): MessageSubscription {
        // TODO (saig0): message subscription doesn't have a key - https://github.com/zeebe-io/zeebe/issues/2805
        val key = record.metadata.position
        return MessageSubscription(
                key = key,
                messageName = record.messageName,
                messageCorrelationKey = record.correlationKey,
                workflowInstanceKey = record.workflowInstanceKey,
                elementInstanceKey = record.elementInstanceKey,
                elementId = null,
                workflowKey = null
        );
    }

    private fun importMessageStartEventSubscriptionRecord(record: Schema.MessageStartEventSubscriptionRecord) {
        val entity = messageSubscriptionRepository
                .findByWorkflowKeyAndMessageName(record.workflowKey, record.messageName)
                ?: (createMessageSubscription(record))

        when (record.metadata.intent) {
            "OPENED" -> entity.state = MessageSubscriptionState.OPENED
            "CLOSED" -> entity.state = MessageSubscriptionState.CLOSED
        }

        entity.timestamp = record.metadata.timestamp

        messageSubscriptionRepository.save(entity)
    }

    public fun createMessageSubscription(record: Schema.MessageStartEventSubscriptionRecord): MessageSubscription {
        // TODO (saig0): message subscription doesn't have a key - https://github.com/zeebe-io/zeebe/issues/2805
        val key = record.metadata.position
        return MessageSubscription(
                key = key,
                messageName = record.messageName,
                workflowKey = record.workflowKey,
                elementId = record.startEventId,
                elementInstanceKey = null,
                workflowInstanceKey = null,
                messageCorrelationKey = null
        );
    }

    private fun importWorkflowInstanceSubscriptionRecord(record: Schema.WorkflowInstanceSubscriptionRecord) {
        when (record.metadata.intent) {
            "CORRELATED" -> importMessageCorrelation(record)
        }
    }

    private fun importMessageCorrelation(record: Schema.WorkflowInstanceSubscriptionRecord) {

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
}

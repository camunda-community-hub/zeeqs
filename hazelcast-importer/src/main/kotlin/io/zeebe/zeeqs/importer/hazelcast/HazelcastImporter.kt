package io.zeebe.zeeqs.importer.hazelcast

import com.google.protobuf.Struct
import com.google.protobuf.Value
import com.hazelcast.client.HazelcastClient
import com.hazelcast.client.config.ClientConfig
import io.zeebe.exporter.proto.Schema
import io.zeebe.exporter.proto.Schema.RecordMetadata.RecordType
import io.zeebe.hazelcast.connect.java.ZeebeHazelcast
import io.zeebe.zeeqs.data.entity.*
import io.zeebe.zeeqs.data.repository.*
import org.springframework.stereotype.Component
import java.time.Duration
import javax.persistence.Id


@Component
class HazelcastImporter(
    val hazelcastConfigRepository: HazelcastConfigRepository,
    val processRepository: ProcessRepository,
    val processInstanceRepository: ProcessInstanceRepository,
    val elementInstanceRepository: ElementInstanceRepository,
    val elementInstanceStateTransitionRepository: ElementInstanceStateTransitionRepository,
    val variableRepository: VariableRepository,
    val variableUpdateRepository: VariableUpdateRepository,
    val jobRepository: JobRepository,
    val incidentRepository: IncidentRepository,
    val timerRepository: TimerRepository,
    val messageRepository: MessageRepository,
    val messageVariableRepository: MessageVariableRepository,
    val messageSubscriptionRepository: MessageSubscriptionRepository,
    val messageCorrelationRepository: MessageCorrelationRepository,
    val errorRepository: ErrorRepository
) {

    var zeebeHazelcast: ZeebeHazelcast? = null

    fun start(hazelcastProperties: HazelcastProperties) {

        val hazelcastConnection = hazelcastProperties.connection
        val hazelcastConnectionTimeout = Duration.parse(hazelcastProperties.connectionTimeout)
        val hazelcastRingbuffer = hazelcastProperties.ringbuffer
        val hazelcastConnectionInitialBackoff =
            Duration.parse(hazelcastProperties.connectionInitialBackoff)
        val hazelcastConnectionBackoffMultiplier = hazelcastProperties.connectionBackoffMultiplier
        val hazelcastConnectionMaxBackoff = Duration.parse(hazelcastProperties.connectionMaxBackoff)

        val hazelcastConfig = hazelcastConfigRepository.findById(hazelcastConnection)
            .orElse(
                HazelcastConfig(
                    id = hazelcastConnection,
                    sequence = -1
                )
            )

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
        connectionRetryConfig.initialBackoffMillis =
            hazelcastConnectionInitialBackoff.toMillis().toInt()
        connectionRetryConfig.multiplier = hazelcastConnectionBackoffMultiplier
        connectionRetryConfig.maxBackoffMillis = hazelcastConnectionMaxBackoff.toMillis().toInt()

        val hazelcast = HazelcastClient.newHazelcastClient(clientConfig)

        val builder = ZeebeHazelcast.newBuilder(hazelcast).name(hazelcastRingbuffer)
            .addProcessListener {
                it.takeIf { it.metadata.recordType == RecordType.EVENT }?.let(this::importProcess)
            }
            .addProcessInstanceListener {
                it.takeIf { it.metadata.recordType == RecordType.EVENT }
                    ?.let(this::importProcessInstanceRecord)
            }
            .addVariableListener {
                it.takeIf { it.metadata.recordType == RecordType.EVENT }
                    ?.let(this::importVariableRecord)
            }
            .addJobListener {
                it.takeIf { it.metadata.recordType == RecordType.EVENT }?.let(this::importJobRecord)
            }
            .addIncidentListener {
                it.takeIf { it.metadata.recordType == RecordType.EVENT }
                    ?.let(this::importIncidentRecord)
            }
            .addTimerListener {
                it.takeIf { it.metadata.recordType == RecordType.EVENT }
                    ?.let(this::importTimerRecord)
            }
            .addMessageListener {
                it.takeIf { it.metadata.recordType == RecordType.EVENT }
                    ?.let(this::importMessageRecord)
            }
            .addMessageSubscriptionListener {
                it.takeIf { it.metadata.recordType == RecordType.EVENT }
                    ?.let(this::importMessageSubscriptionRecord)
            }
            .addMessageStartEventSubscriptionListener {
                it.takeIf { it.metadata.recordType == RecordType.EVENT }
                    ?.let(this::importMessageStartEventSubscriptionRecord)
            }
            .addProcessMessageSubscriptionListener {
                it.takeIf { it.metadata.recordType == RecordType.EVENT }
                    ?.let(this::importProcessMessageSubscriptionRecord)
            }
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

    private fun getPartitionIdWithPosition(metadata: Schema.RecordMetadata) =
            "${metadata.partitionId}-${metadata.position}"

    private fun importProcess(process: Schema.ProcessRecord) {
        val entity = processRepository
            .findById(process.processDefinitionKey)
            .orElse(createProcess(process))

        processRepository.save(entity)
    }

    private fun createProcess(process: Schema.ProcessRecord): Process {
        return Process(
            key = process.processDefinitionKey,
            bpmnProcessId = process.bpmnProcessId,
            version = process.version,
            bpmnXML = process.resource.toStringUtf8(),
            deployTime = process.metadata.timestamp,
            resourceName = process.resourceName,
            checksum = process.checksum
        )
    }

    private fun importProcessInstanceRecord(record: Schema.ProcessInstanceRecord) {
        if (record.processInstanceKey == record.metadata.key) {
            importProcessInstance(record)
        }

        importElementInstance(record)
        importElementInstanceStateTransition(record)
    }

    private fun importProcessInstance(record: Schema.ProcessInstanceRecord) {
        val entity = processInstanceRepository
            .findById(record.processInstanceKey)
            .orElse(createProcessInstance(record))

        when (record.metadata.intent) {
            "ELEMENT_ACTIVATED" -> {
                entity.startTime = record.metadata.timestamp
                entity.state = ProcessInstanceState.ACTIVATED
            }
            "ELEMENT_COMPLETED" -> {
                entity.endTime = record.metadata.timestamp
                entity.state = ProcessInstanceState.COMPLETED
            }
            "ELEMENT_TERMINATED" -> {
                entity.endTime = record.metadata.timestamp
                entity.state = ProcessInstanceState.TERMINATED
            }
        }

        processInstanceRepository.save(entity)
    }

    private fun createProcessInstance(record: Schema.ProcessInstanceRecord): ProcessInstance {
        return ProcessInstance(
            key = record.processInstanceKey,
            position = record.metadata.position,
            bpmnProcessId = record.bpmnProcessId,
            version = record.version,
            processDefinitionKey = record.processDefinitionKey,
            parentProcessInstanceKey = record.parentProcessInstanceKey.takeIf { it > 0 },
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
            "UNSPECIFIED" -> BpmnElementType.UNSPECIFIED
            "BOUNDARY_EVENT" -> BpmnElementType.BOUNDARY_EVENT
            "CALL_ACTIVITY" -> BpmnElementType.CALL_ACTIVITY
            "END_EVENT" -> BpmnElementType.END_EVENT
            "EVENT_BASED_GATEWAY" -> BpmnElementType.EVENT_BASED_GATEWAY
            "EXCLUSIVE_GATEWAY" -> BpmnElementType.EXCLUSIVE_GATEWAY
            "INTERMEDIATE_CATCH_EVENT" -> BpmnElementType.INTERMEDIATE_CATCH_EVENT
            "INTERMEDIATE_THROW_EVENT" -> BpmnElementType.INTERMEDIATE_THROW_EVENT
            "PARALLEL_GATEWAY" -> BpmnElementType.PARALLEL_GATEWAY
            "PROCESS" -> BpmnElementType.PROCESS
            "RECEIVE_TASK" -> BpmnElementType.RECEIVE_TASK
            "SEQUENCE_FLOW" -> BpmnElementType.SEQUENCE_FLOW
            "SERVICE_TASK" -> BpmnElementType.SERVICE_TASK
            "START_EVENT" -> BpmnElementType.START_EVENT
            "SUB_PROCESS" -> BpmnElementType.SUB_PROCESS
            "EVENT_SUB_PROCESS" -> BpmnElementType.EVENT_SUB_PROCESS
            "MULTI_INSTANCE_BODY" -> BpmnElementType.MULTI_INSTANCE_BODY
            "USER_TASK" -> BpmnElementType.USER_TASK
            "MANUAL_TASK" -> BpmnElementType.MANUAL_TASK
            "BUSINESS_RULE_TASK" -> BpmnElementType.BUSINESS_RULE_TASK
            "SCRIPT_TASK" -> BpmnElementType.SCRIPT_TASK
            "SEND_TASK" -> BpmnElementType.SEND_TASK
            else -> BpmnElementType.UNKNOWN
        }

        return ElementInstance(
            key = record.metadata.key,
            position = record.metadata.position,
            elementId = record.elementId,
            bpmnElementType = bpmnElementType,
            processInstanceKey = record.processInstanceKey,
            processDefinitionKey = record.processDefinitionKey,
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
            "SEQUENCE_FLOW_TAKEN" -> ElementInstanceState.TAKEN
            else -> ElementInstanceState.ACTIVATING
        }
    }

    private fun importElementInstanceStateTransition(record: Schema.ProcessInstanceRecord) {

        val state = getElementInstanceState(record)

        val partitionIdWithPosition = getPartitionIdWithPosition(record.metadata)
        val entity = elementInstanceStateTransitionRepository
            .findById(partitionIdWithPosition)
            .orElse(
                ElementInstanceStateTransition(
                    partitionIdWithPosition = partitionIdWithPosition,
                    elementInstanceKey = record.metadata.key,
                    timestamp = record.metadata.timestamp,
                    state = state
                )
            )

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
            position = record.metadata.position,
            name = record.name,
            value = record.value,
            processInstanceKey = record.processInstanceKey,
            scopeKey = record.scopeKey,
            timestamp = record.metadata.timestamp
        )
    }

    private fun importVariableUpdate(record: Schema.VariableRecord) {

        val partitionIdWithPosition = getPartitionIdWithPosition(record.metadata)
        val entity = variableUpdateRepository
            .findById(partitionIdWithPosition)
            .orElse(
                VariableUpdate(
                    partitionIdWithPosition = partitionIdWithPosition,
                    variableKey = record.metadata.key,
                    name = record.name,
                    value = record.value,
                    processInstanceKey = record.processInstanceKey,
                    scopeKey = record.scopeKey,
                    timestamp = record.metadata.timestamp
                )
            )

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
            position = record.metadata.position,
            jobType = record.type,
            processInstanceKey = record.processInstanceKey,
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
            position = record.metadata.position,
            errorType = record.errorType,
            errorMessage = record.errorMessage,
            processInstanceKey = record.processInstanceKey,
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
            position = record.metadata.position,
            dueDate = record.dueDate,
            repetitions = record.repetitions,
            elementId = record.targetElementId,
            processDefinitionKey = record.processDefinitionKey.takeIf { it > 0 },
            processInstanceKey = record.processInstanceKey.takeIf { it > 0 },
            elementInstanceKey = record.elementInstanceKey.takeIf { it > 0 }
        );
    }

    private fun importMessageRecord(record: Schema.MessageRecord) {
        importMessage(record)

        if (record.metadata.intent == "PUBLISHED") {
            importMessageVariables(record)
        }
    }

    private fun importMessage(record: Schema.MessageRecord) {
        val entity = messageRepository
                .findById(record.metadata.key)
                .orElse(createMessage(record))

        when (record.metadata.intent) {
            "PUBLISHED" -> entity.state = MessageState.PUBLISHED
            "EXPIRED" -> entity.state = MessageState.EXPIRED
        }

        entity.timestamp = record.metadata.timestamp

        messageRepository.save(entity)
    }

    private fun createMessage(record: Schema.MessageRecord): Message {
        return Message(
            key = record.metadata.key,
            position = record.metadata.position,
            name = record.name,
            correlationKey = record.correlationKey.takeIf { it.isNotEmpty() },
            messageId = record.messageId.takeIf { it.isNotEmpty() },
            timeToLive = record.timeToLive
        );
    }

    private fun importMessageVariables(record: Schema.MessageRecord) {
        val messageKey = record.metadata.key
        val messagePosition = record.metadata.position

        structToMap(record.variables).forEach{ (name,value) ->
            val id = messageKey.toString() + name

            val entity = messageVariableRepository
                    .findById(id)
                    .orElse(MessageVariable(
                            id = id,
                            name = name,
                            value = value,
                            messageKey = messageKey,
                            position = messagePosition
                    ))

            messageVariableRepository.save(entity)
        }
    }

    private fun structToMap(struct: Struct): Map<String, String> {
        return struct.fieldsMap.mapValues { (_, value) -> valueToString(value) }
    }

    private fun valueToString(value: Value): String {
        return when (value.kindCase) {
            Value.KindCase.NULL_VALUE -> "null"
            Value.KindCase.BOOL_VALUE -> value.boolValue.toString()
            Value.KindCase.NUMBER_VALUE -> value.numberValue.toString()
            Value.KindCase.STRING_VALUE -> "\"${value.stringValue}\""
            Value.KindCase.LIST_VALUE -> value.listValue.valuesList.map { valueToString(it) }.joinToString(separator = ",", prefix = "[", postfix = "]")
            Value.KindCase.STRUCT_VALUE -> value.structValue.fieldsMap.map { (key, value) -> "\"$key\":" + valueToString(value) }.joinToString(separator = ",", prefix = "{", postfix = "}")
            else -> value.toString()
        }
    }

    private fun importMessageSubscriptionRecord(record: Schema.MessageSubscriptionRecord) {
        val entity = messageSubscriptionRepository
            .findById(record.metadata.key)
            .orElse(createMessageSubscription(record))

        when (record.metadata.intent) {
            "CREATED" -> entity.state = MessageSubscriptionState.CREATED
            "CORRELATING" -> entity.state = MessageSubscriptionState.CORRELATING
            "CORRELATED" -> entity.state = MessageSubscriptionState.CORRELATED
            "REJECTED" -> entity.state = MessageSubscriptionState.REJECTED
            "DELETED" -> entity.state = MessageSubscriptionState.DELETED
        }

        entity.timestamp = record.metadata.timestamp

        messageSubscriptionRepository.save(entity)
    }

    private fun createMessageSubscription(record: Schema.MessageSubscriptionRecord): MessageSubscription {
        return MessageSubscription(
            key = record.metadata.key,
            position = record.metadata.position,
            messageName = record.messageName,
            messageCorrelationKey = record.correlationKey,
            processInstanceKey = record.processInstanceKey,
            elementInstanceKey = record.elementInstanceKey,
            elementId = null,
            processDefinitionKey = null
        );
    }

    private fun importMessageStartEventSubscriptionRecord(record: Schema.MessageStartEventSubscriptionRecord) {
        val entity = messageSubscriptionRepository
            .findById(record.metadata.key)
            .orElse(createMessageSubscription(record))

        when (record.metadata.intent) {
            "CREATED" -> entity.state = MessageSubscriptionState.CREATED
            "CORRELATED" -> {
                entity.state = MessageSubscriptionState.CORRELATED
                importMessageCorrelation(record)
            }
            "DELETED" -> entity.state = MessageSubscriptionState.DELETED
        }

        entity.timestamp = record.metadata.timestamp

        messageSubscriptionRepository.save(entity)
    }

    private fun createMessageSubscription(record: Schema.MessageStartEventSubscriptionRecord): MessageSubscription {
        return MessageSubscription(
            key = record.metadata.key,
            position = record.metadata.position,
            messageName = record.messageName,
            processDefinitionKey = record.processDefinitionKey,
            elementId = record.startEventId,
            elementInstanceKey = null,
            processInstanceKey = null,
            messageCorrelationKey = null
        );
    }

    private fun importProcessMessageSubscriptionRecord(record: Schema.ProcessMessageSubscriptionRecord) {
        when (record.metadata.intent) {
            "CORRELATED" -> importMessageCorrelation(record)
        }
    }

    private fun importMessageCorrelation(record: Schema.ProcessMessageSubscriptionRecord) {

        val partitionIdWithPosition = getPartitionIdWithPosition(record.metadata)
        val entity = messageCorrelationRepository
            .findById(partitionIdWithPosition)
            .orElse(
                MessageCorrelation(
                    partitionIdWithPosition = partitionIdWithPosition,
                    messageKey = record.messageKey,
                    messageName = record.messageName,
                    elementInstanceKey = record.elementInstanceKey,
                    processInstanceKey = record.processInstanceKey,
                    elementId = record.elementId,
                    processDefinitionKey = null,
                    timestamp = record.metadata.timestamp
                )
            )

        messageCorrelationRepository.save(entity)
    }

    private fun importMessageCorrelation(record: Schema.MessageStartEventSubscriptionRecord) {

        val partitionIdWithPosition = getPartitionIdWithPosition(record.metadata)
        val entity = messageCorrelationRepository
            .findById(partitionIdWithPosition)
            .orElse(
                MessageCorrelation(
                    partitionIdWithPosition = partitionIdWithPosition,
                    messageKey = record.messageKey,
                    messageName = record.messageName,
                    elementInstanceKey = null,
                    processInstanceKey = record.processInstanceKey,
                    elementId = record.startEventId,
                    processDefinitionKey = record.processDefinitionKey,
                    timestamp = record.metadata.timestamp
                )
            )

        messageCorrelationRepository.save(entity)
    }

    private fun importError(record: Schema.ErrorRecord) {

        val entity = errorRepository.findById(record.metadata.position)
            .orElse(Error(
                position = record.metadata.position,
                errorEventPosition = record.errorEventPosition,
                exceptionMessage = record.exceptionMessage,
                stacktrace = record.stacktrace,
                processInstanceKey = record.processInstanceKey.takeIf { it > 0 }
            ))

        errorRepository.save(entity)
    }

}

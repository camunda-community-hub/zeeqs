package io.zeebe.zeeqs.importer.hazelcast

import com.hazelcast.client.HazelcastClient
import com.hazelcast.client.config.ClientConfig
import io.zeebe.exporter.proto.Schema
import io.zeebe.hazelcast.connect.java.ZeebeHazelcast
import io.zeebe.zeeqs.data.entity.Variable
import io.zeebe.zeeqs.data.entity.VariableUpdate
import io.zeebe.zeeqs.data.entity.WorkflowInstance
import io.zeebe.zeeqs.data.entity.WorkflowInstanceState
import io.zeebe.zeeqs.data.repository.VariableRepository
import io.zeebe.zeeqs.data.repository.VariableUpdateRepository
import io.zeebe.zeeqs.data.repository.WorkflowInstanceRepository
import org.springframework.stereotype.Component

@Component
class HazelcastImporter(
        val workflowInstanceRepository: WorkflowInstanceRepository,
        val variableRepository: VariableRepository,
        val variableUpdateRepository: VariableUpdateRepository) {

    fun start(hazelcastConnection: String) {

        val clientConfig = ClientConfig()
        clientConfig.networkConfig.addAddress(hazelcastConnection)

        val hazelcast = HazelcastClient.newHazelcastClient(clientConfig)

        val zeebeHazelcast = ZeebeHazelcast(hazelcast)

        zeebeHazelcast.addWorkflowInstanceListener(this::importWorkflowInstanceRecord)
        zeebeHazelcast.addVariableListener(this::importVariableRecord)
    }

    private fun importWorkflowInstanceRecord(record: Schema.WorkflowInstanceRecord) {
        if (record.workflowInstanceKey == record.metadata.key) {
            importWorkflowInstance(record)
        }
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

    private fun createWorkflowInstance(record: Schema.WorkflowInstanceRecord): WorkflowInstance {
        return WorkflowInstance(
                key = record.workflowInstanceKey,
                bpmnProcessId = record.bpmnProcessId,
                version = record.version,
                workflowKey = record.workflowKey,
                parentWorkflowInstanceKey = if (record.parentWorkflowInstanceKey > 0) record.parentWorkflowInstanceKey else null,
                parentElementInstanceKey = if (record.parentElementInstanceKey > 0) record.parentElementInstanceKey else null
        )
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
                workflowInstanceKey = record.workflowInstanceKey,
                scopeKey = record.scopeKey,
                timestamp = record.metadata.timestamp
        )
    }

    private fun importVariableUpdate(record: Schema.VariableRecord) {

        val entity = VariableUpdate(
                position = record.metadata.position,
                variableKey = record.metadata.key,
                name = record.name,
                value = record.value,
                workflowInstanceKey = record.workflowInstanceKey,
                scopeKey = record.scopeKey,
                timestamp = record.metadata.timestamp
        )

        variableUpdateRepository.save(entity)
    }
}

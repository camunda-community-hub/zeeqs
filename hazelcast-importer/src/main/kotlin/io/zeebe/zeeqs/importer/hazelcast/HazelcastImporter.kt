package io.zeebe.zeeqs.importer.hazelcast

import com.hazelcast.client.HazelcastClient
import com.hazelcast.client.config.ClientConfig
import io.zeebe.exporter.proto.Schema
import io.zeebe.hazelcast.connect.java.ZeebeHazelcast
import io.zeebe.zeeqs.data.entity.Variable
import io.zeebe.zeeqs.data.entity.WorkflowInstance
import io.zeebe.zeeqs.data.entity.WorkflowInstanceState
import io.zeebe.zeeqs.data.repository.VariableRepository
import io.zeebe.zeeqs.data.repository.WorkflowInstanceRepository
import org.springframework.stereotype.Component

@Component
class HazelcastImporter(
        val workflowInstanceRepository: WorkflowInstanceRepository,
        val variableRepository: VariableRepository) {

    fun start(hazelcastConnection: String) {

        val clientConfig = ClientConfig()
        clientConfig.networkConfig.addAddress(hazelcastConnection)

        val hazelcast = HazelcastClient.newHazelcastClient(clientConfig)

        val zeebeHazelcast = ZeebeHazelcast(hazelcast)

        zeebeHazelcast.addWorkflowInstanceListener(this::importWorkflowInstanceRecord)
        zeebeHazelcast.addVariableListener(this::importVariable)
    }

    private fun importWorkflowInstanceRecord(record: Schema.WorkflowInstanceRecord) {
        if (record.workflowInstanceKey == record.metadata.key) {
            importWorkflowInstance(record)
        }
    }

    private fun importWorkflowInstance(record: Schema.WorkflowInstanceRecord) {
        val entity = workflowInstanceRepository
                .findById(record.workflowInstanceKey)
                .orElse(
                        WorkflowInstance(
                                key = record.workflowInstanceKey,
                                bpmnProcessId = record.bpmnProcessId,
                                version = record.version,
                                workflowKey = record.workflowKey,
                                parentWorkflowInstanceKey = if (record.parentWorkflowInstanceKey > 0) record.parentWorkflowInstanceKey else null,
                                parentElementInstanceKey = if (record.parentElementInstanceKey > 0) record.parentElementInstanceKey else null
                        ))

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

    private fun importVariable(variable: Schema.VariableRecord) {

        val entity = variableRepository.findById(variable.metadata.key)
                .orElse(Variable(
                        variable.metadata.key,
                        variable.name,
                        variable.value,
                        variable.workflowInstanceKey
                ))

        entity.value = variable.value

        variableRepository.save(entity)
    }
}

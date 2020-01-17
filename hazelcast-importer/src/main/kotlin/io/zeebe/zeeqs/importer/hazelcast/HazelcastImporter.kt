package io.zeebe.zeeqs.importer.hazelcast

import com.hazelcast.client.HazelcastClient
import com.hazelcast.client.config.ClientConfig
import io.zeebe.exporter.proto.Schema
import io.zeebe.hazelcast.connect.java.ZeebeHazelcast
import io.zeebe.zeeqs.data.entity.Variable
import io.zeebe.zeeqs.data.entity.WorkflowInstance
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

        zeebeHazelcast.addWorkflowInstanceListener(this::importWorkflowInstance)
        zeebeHazelcast.addVariableListener(this::importVariable)
    }

    fun importWorkflowInstance(workflowInstance: Schema.WorkflowInstanceRecord) {
        if (workflowInstance.workflowInstanceKey == workflowInstance.metadata.key) {

            val entity = workflowInstanceRepository
                    .findById(workflowInstance.workflowInstanceKey)
                    .orElse(WorkflowInstance(
                            workflowInstance.workflowInstanceKey,
                            workflowInstance.bpmnProcessId,
                            workflowInstance.version,
                            workflowInstance.workflowKey
                    ))



            workflowInstanceRepository.save(entity)
        }
    }

    fun importVariable(variable: Schema.VariableRecord) {

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

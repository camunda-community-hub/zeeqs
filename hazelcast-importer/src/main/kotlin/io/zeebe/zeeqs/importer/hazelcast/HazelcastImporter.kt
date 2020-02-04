package io.zeebe.zeeqs.importer.hazelcast

import com.hazelcast.client.HazelcastClient
import com.hazelcast.client.config.ClientConfig
import io.zeebe.exporter.proto.Schema
import io.zeebe.hazelcast.connect.java.ZeebeHazelcast
import io.zeebe.zeeqs.data.entity.*
import io.zeebe.zeeqs.data.repository.*
import org.springframework.stereotype.Component

@Component
class HazelcastImporter(
        val workflowRepository: WorkflowRepository,
        val workflowInstanceRepository: WorkflowInstanceRepository,
        val variableRepository: VariableRepository,
        val variableUpdateRepository: VariableUpdateRepository,
        val jobRepository: JobRepository) {

    fun start(hazelcastConnection: String) {

        val clientConfig = ClientConfig()
        clientConfig.networkConfig.addAddress(hazelcastConnection)

        val hazelcast = HazelcastClient.newHazelcastClient(clientConfig)

        val zeebeHazelcast = ZeebeHazelcast(hazelcast)

        zeebeHazelcast.addDeploymentListener(this::importDeploymentRecord)
        zeebeHazelcast.addWorkflowInstanceListener(this::importWorkflowInstanceRecord)
        zeebeHazelcast.addVariableListener(this::importVariableRecord)
        zeebeHazelcast.addJobListener(this::importJobRecord)
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

    private fun createWorkflow(deployment: Schema.DeploymentRecord,
                               workflow: Schema.DeploymentRecord.Workflow,
                               resource: Schema.DeploymentRecord.Resource): Workflow {
        return Workflow(
                key = workflow.workflowKey,
                bpmnProcessId = workflow.bpmnProcessId,
                version = workflow.version,
                bpmnXML = resource.resource.toStringUtf8(),
                timestamp = deployment.metadata.timestamp
        )
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
                parentWorkflowInstanceKey = record.parentWorkflowInstanceKey.takeIf { it > 0 },
                parentElementInstanceKey = record.parentElementInstanceKey.takeIf { it > 0 }
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

    private fun importJobRecord(record: Schema.JobRecord) {
        val entity = jobRepository
                .findById(record.metadata.key)
                .orElse(createJob(record))

        when (record.metadata.intent) {
            "CREATED", "TIMED_OUT", "RETRIES_UPDATED" -> entity.state = JobState.ACTIVATABLE
            "ACTIVATED" -> entity.state = JobState.ACTIVATED
            "FAILED" -> entity.state = JobState.FAILED
            "COMPLETED" -> entity.state = JobState.COMPLETED
            "CANCELED" -> entity.state = JobState.CANCELED
            "ERROR_THROWN" -> entity.state = JobState.ERROR_THROWN
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
                workflowInstanceKey = record.workflowInstanceKey,
                elementInstanceKey = record.elementInstanceKey
        )
    }
}

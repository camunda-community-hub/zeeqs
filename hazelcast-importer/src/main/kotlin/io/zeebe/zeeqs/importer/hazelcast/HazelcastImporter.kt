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
        val elementInstanceRepository: ElementInstanceRepository,
        val elementInstanceStateTransitionRepository: ElementInstanceStateTransitionRepository,
        val variableRepository: VariableRepository,
        val variableUpdateRepository: VariableUpdateRepository,
        val jobRepository: JobRepository,
        val incidentRepository: IncidentRepository) {

    fun start(hazelcastConnection: String) {

        val clientConfig = ClientConfig()
        clientConfig.networkConfig.addAddress(hazelcastConnection)

        val hazelcast = HazelcastClient.newHazelcastClient(clientConfig)

        val zeebeHazelcast = ZeebeHazelcast(hazelcast)

        zeebeHazelcast.addDeploymentListener(this::importDeploymentRecord)
        zeebeHazelcast.addWorkflowInstanceListener(this::importWorkflowInstanceRecord)
        zeebeHazelcast.addVariableListener(this::importVariableRecord)
        zeebeHazelcast.addJobListener(this::importJobRecord)
        zeebeHazelcast.addIncidentListener(this::importIncidentRecord)
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

    private fun createElementInstance(record: Schema.WorkflowInstanceRecord): ElementInstance {

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

    private fun getElementInstanceState(record: Schema.WorkflowInstanceRecord): ElementInstanceState {
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

        val entity = ElementInstanceStateTransition(
                position = record.metadata.position,
                elementInstanceKey = record.metadata.key,
                timestamp = record.metadata.timestamp,
                state = state
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
                workflowInstanceKey = record.workflowInstanceKey,
                elementInstanceKey = record.elementInstanceKey,
                jobKey = record.jobKey.takeIf { it > 0 }
        )
    }
}

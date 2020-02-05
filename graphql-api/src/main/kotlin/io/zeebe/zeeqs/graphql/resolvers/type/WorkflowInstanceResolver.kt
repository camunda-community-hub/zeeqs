package io.zeebe.zeeqs.data.resolvers

import com.coxautodev.graphql.tools.GraphQLResolver
import io.zeebe.zeeqs.data.entity.*
import io.zeebe.zeeqs.data.repository.*
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class WorkflowInstanceResolver(
        val workflowInstanceRepository: WorkflowInstanceRepository,
        val variableRepository: VariableRepository,
        val workflowRepository: WorkflowRepository,
        val jobRepository: JobRepository,
        val incidentRepository: IncidentRepository,
        val elementInstanceRepository: ElementInstanceRepository
) : GraphQLResolver<WorkflowInstance> {

    fun variables(workflowInstance: WorkflowInstance): List<Variable> {
        return variableRepository.findByWorkflowInstanceKey(workflowInstance.key)
    }

    fun jobs(workflowInstance: WorkflowInstance): List<Job> {
        return jobRepository.findByWorkflowInstanceKey(workflowInstance.key)
    }

    fun workflow(workflowInstance: WorkflowInstance): Workflow? {
        return workflowRepository.findByIdOrNull(workflowInstance.workflowKey)
    }

    fun incidents(workflowInstance: WorkflowInstance): List<Incident> {
        return incidentRepository.findByWorkflowInstanceKey(workflowInstance.key)
    }

    fun parentElementInstance(workflowInstance: WorkflowInstance): ElementInstance? {
        return workflowInstance.parentElementInstanceKey?.let { elementInstanceRepository.findByIdOrNull(it) }
    }

    fun childWorkflowInstances(workflowInstance: WorkflowInstance): List<WorkflowInstance> {
        return workflowInstanceRepository.findByParentWorkflowInstanceKey(workflowInstance.key)
    }

    fun elementInstances(workflowInstance: WorkflowInstance): List<ElementInstance> {
        return elementInstanceRepository.findByWorkflowInstanceKey(workflowInstance.key)
    }

}
package io.zeebe.zeeqs.data.resolvers

import com.coxautodev.graphql.tools.GraphQLQueryResolver
import io.zeebe.zeeqs.data.entity.*
import io.zeebe.zeeqs.data.repository.*
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class WorkflowInstanceQueryResolver(
        val workflowInstanceRepository: WorkflowInstanceRepository,
        val variableRepository: VariableRepository,
        val variableUpdateRepository: VariableUpdateRepository,
        val workflowRepository: WorkflowRepository,
        val jobRepository: JobRepository
) : GraphQLQueryResolver {

    fun getWorkflowInstances(count: Int, offset: Int): List<WorkflowInstance> {
        val workflowInstances = workflowInstanceRepository.findAll(PageRequest.of(offset, count))

        for (workflowInstance in workflowInstances) {
            transformWorkflowInstance(workflowInstance)
        }

        return workflowInstances.toList().toList()
    }

    private fun transformWorkflowInstance(workflowInstance: WorkflowInstance): WorkflowInstance {
        workflowInstance.workflow = getWorkflow(workflowInstance.workflowKey)
        workflowInstance.variables = getVariables(workflowInstance.key)
        workflowInstance.jobs = getJobs(workflowInstance.key)
        return workflowInstance
    }

    private fun getVariables(workflowInstanceKey: Long): List<Variable> {
        val variables = variableRepository.findByWorkflowInstanceKey(workflowInstanceKey)

        for (variable in variables) {
            variable.updates = getVariableUpdates(variable.key)
        }

        return variables
    }

    private fun getVariableUpdates(variableKey: Long): List<VariableUpdate> {
        return variableUpdateRepository.findByVariableKey(variableKey)
    }

    private fun getJobs(workflowInstanceKey: Long): List<Job> {
        return jobRepository.findByWorkflowInstanceKey(workflowInstanceKey)
    }

    private fun getWorkflow(workflowKey: Long): Workflow? {
        return workflowRepository.findByIdOrNull(workflowKey)
    }

    fun workflowInstance(key: Long): WorkflowInstance? {
        return workflowInstanceRepository.findByIdOrNull(key)?.let { transformWorkflowInstance(it) }
    }

}
package io.zeebe.zeeqs.data.resolvers

import com.coxautodev.graphql.tools.GraphQLQueryResolver
import io.zeebe.zeeqs.data.entity.Variable
import io.zeebe.zeeqs.data.entity.VariableUpdate
import io.zeebe.zeeqs.data.entity.Workflow
import io.zeebe.zeeqs.data.entity.WorkflowInstance
import io.zeebe.zeeqs.data.repository.VariableRepository
import io.zeebe.zeeqs.data.repository.VariableUpdateRepository
import io.zeebe.zeeqs.data.repository.WorkflowInstanceRepository
import io.zeebe.zeeqs.data.repository.WorkflowRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class WorkflowInstanceQueryResolver(
        val workflowInstanceRepository: WorkflowInstanceRepository,
        val variableRepository: VariableRepository,
        val variableUpdateRepository: VariableUpdateRepository,
        val workflowRepository: WorkflowRepository
) : GraphQLQueryResolver {

    fun getWorkflowInstances(count: Int, offset: Int): List<WorkflowInstance> {
        val workflowInstances = workflowInstanceRepository.findAll(PageRequest.of(offset, count))

        for (workflowInstance in workflowInstances) {
            transformWorkflowInstance(workflowInstance)
        }

        return workflowInstances.toList().toList()
    }

    private fun transformWorkflowInstance(workflowInstance: WorkflowInstance): WorkflowInstance {
        workflowInstance.variables = getVariables(workflowInstance.key)
        workflowInstance.workflow = getWorkflow(workflowInstance.workflowKey)
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

    private fun getWorkflow(workflowKey: Long): Workflow? {
        return workflowRepository.findByIdOrNull(workflowKey)
    }

    fun workflowInstance(key: Long): WorkflowInstance? {
        return workflowInstanceRepository.findByIdOrNull(key)?.let { transformWorkflowInstance(it) }
    }

}
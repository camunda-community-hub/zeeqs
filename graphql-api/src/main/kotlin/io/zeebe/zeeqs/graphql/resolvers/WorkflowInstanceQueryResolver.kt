package io.zeebe.zeeqs.data.resolvers

import com.coxautodev.graphql.tools.GraphQLQueryResolver
import io.zeebe.zeeqs.data.entity.Variable
import io.zeebe.zeeqs.data.entity.VariableUpdate
import io.zeebe.zeeqs.data.entity.WorkflowInstance
import io.zeebe.zeeqs.data.repository.VariableRepository
import io.zeebe.zeeqs.data.repository.VariableUpdateRepository
import io.zeebe.zeeqs.data.repository.WorkflowInstanceRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class WorkflowInstanceQueryResolver(
        val workflowInstanceRepository: WorkflowInstanceRepository,
        val variableRepository: VariableRepository,
        val variableUpdateRepository: VariableUpdateRepository
) : GraphQLQueryResolver {

    fun getWorkflowInstances(count: Int, offset: Int): List<WorkflowInstance> {
        val workflowInstances = workflowInstanceRepository.findAll(PageRequest.of(offset, count))

        for (workflowInstance in workflowInstances) {
            workflowInstance.variables = getVariables(workflowInstance.key)
        }

        return workflowInstances.toList().toList()
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

    fun workflowInstance(key: Long): WorkflowInstance? {
        return workflowInstanceRepository.findByIdOrNull(key)
    }

}
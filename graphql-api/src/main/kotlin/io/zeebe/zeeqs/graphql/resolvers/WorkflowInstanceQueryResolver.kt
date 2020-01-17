package io.zeebe.zeeqs.data.resolvers

import com.coxautodev.graphql.tools.GraphQLQueryResolver
import io.zeebe.zeeqs.data.entity.Variable
import io.zeebe.zeeqs.data.entity.WorkflowInstance
import io.zeebe.zeeqs.data.repository.VariableRepository
import io.zeebe.zeeqs.data.repository.WorkflowInstanceRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component

@Component
class WorkflowInstanceQueryResolver(
        val workflowInstanceRepository: WorkflowInstanceRepository,
        val variableRepository: VariableRepository
) : GraphQLQueryResolver {

    fun getWorkflowInstances(count: Int, offset: Int): List<WorkflowInstance> {
        val workflowInstances = workflowInstanceRepository.findAll(PageRequest.of(offset, count))

        for (workflowInstance in workflowInstances) {
            workflowInstance.variables = getVariables(workflowInstance.key)
        }

        return workflowInstances.toList().toList()
    }

    private fun getVariables(workflowInstanceKey: Long): List<Variable> {
        return variableRepository.findByWorkflowInstanceKey(workflowInstanceKey);
    }

}
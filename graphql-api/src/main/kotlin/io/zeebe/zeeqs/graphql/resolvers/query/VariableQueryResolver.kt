package io.zeebe.zeeqs.data.resolvers

import com.coxautodev.graphql.tools.GraphQLQueryResolver
import io.zeebe.zeeqs.data.entity.Variable
import io.zeebe.zeeqs.data.repository.VariableRepository
import org.springframework.stereotype.Component

@Component
class VariableQueryResolver(
        val variableRepository: VariableRepository
) : GraphQLQueryResolver {

    fun getVariables(workflowInstanceKey: Long): List<Variable> {
        return variableRepository.findByWorkflowInstanceKey(workflowInstanceKey);
    }

}
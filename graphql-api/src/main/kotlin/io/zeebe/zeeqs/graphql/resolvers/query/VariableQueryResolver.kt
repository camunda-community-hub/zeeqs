package io.zeebe.zeeqs.data.resolvers

import graphql.kickstart.tools.GraphQLQueryResolver
import io.zeebe.zeeqs.data.entity.Variable
import io.zeebe.zeeqs.data.repository.VariableRepository
import org.springframework.stereotype.Component

@Component
class VariableQueryResolver(
        val variableRepository: VariableRepository
) : GraphQLQueryResolver {

    fun getVariables(processInstanceKey: Long): List<Variable> {
        return variableRepository.findByProcessInstanceKey(processInstanceKey);
    }

}
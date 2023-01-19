package io.zeebe.zeeqs.graphql.resolvers.query

import io.zeebe.zeeqs.data.entity.Variable
import io.zeebe.zeeqs.data.repository.VariableRepository
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class VariableQueryResolver(
        val variableRepository: VariableRepository
) {

    @QueryMapping
    fun getVariables(@Argument processInstanceKey: Long): List<Variable> {
        return variableRepository.findByProcessInstanceKey(processInstanceKey);
    }

}
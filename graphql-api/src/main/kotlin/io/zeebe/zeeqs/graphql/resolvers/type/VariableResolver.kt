package io.zeebe.zeeqs.data.resolvers

import com.coxautodev.graphql.tools.GraphQLResolver
import io.zeebe.zeeqs.data.entity.Variable
import io.zeebe.zeeqs.data.entity.VariableUpdate
import io.zeebe.zeeqs.data.repository.VariableUpdateRepository
import org.springframework.stereotype.Component

@Component
class VariableResolver(
        val variableUpdateRepository: VariableUpdateRepository
) : GraphQLResolver<Variable> {

    fun updates(variable: Variable): List<VariableUpdate> {
        return variableUpdateRepository.findByVariableKey(variable.key)
    }
}
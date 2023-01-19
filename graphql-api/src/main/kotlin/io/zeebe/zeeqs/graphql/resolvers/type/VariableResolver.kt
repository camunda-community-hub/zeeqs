package io.zeebe.zeeqs.graphql.resolvers.type

import io.zeebe.zeeqs.data.entity.ElementInstance
import io.zeebe.zeeqs.data.entity.Variable
import io.zeebe.zeeqs.data.entity.VariableUpdate
import io.zeebe.zeeqs.data.repository.ElementInstanceRepository
import io.zeebe.zeeqs.data.repository.VariableUpdateRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class VariableResolver(
        val variableUpdateRepository: VariableUpdateRepository,
        val elementInstanceRepository: ElementInstanceRepository
) {

    @SchemaMapping(typeName = "Variable", field = "timestamp")
    fun timestamp(
            variable: Variable,
            @Argument zoneId: String
    ): String? {
        return variable.timestamp.let { ResolverExtension.timestampToString(it, zoneId) }
    }

    @SchemaMapping(typeName = "Variable", field = "updates")
    fun updates(variable: Variable): List<VariableUpdate> {
        return variableUpdateRepository.findByVariableKey(variable.key)
    }

    @SchemaMapping(typeName = "Variable", field = "scope")
    fun scope(variable: Variable): ElementInstance? {
        return elementInstanceRepository.findByIdOrNull(variable.scopeKey)
    }
}
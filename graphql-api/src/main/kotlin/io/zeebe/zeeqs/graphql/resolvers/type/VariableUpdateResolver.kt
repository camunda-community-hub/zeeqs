package io.zeebe.zeeqs.graphql.resolvers.type

import io.zeebe.zeeqs.data.entity.VariableUpdate
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class VariableUpdateResolver {

    @SchemaMapping(typeName = "VariableUpdate", field = "timestamp")
    fun timestamp(
            variableUpdate: VariableUpdate,
            @Argument zoneId: String
    ): String? {
        return variableUpdate.timestamp.let { ResolverExtension.timestampToString(it, zoneId) }
    }

}
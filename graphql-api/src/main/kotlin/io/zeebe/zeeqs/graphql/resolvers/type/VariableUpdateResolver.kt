package io.zeebe.zeeqs.data.resolvers

import com.coxautodev.graphql.tools.GraphQLResolver
import io.zeebe.zeeqs.data.entity.VariableUpdate
import io.zeebe.zeeqs.graphql.resolvers.type.ResolverExtension
import org.springframework.stereotype.Component

@Component
class VariableUpdateResolver : GraphQLResolver<VariableUpdate> {

    fun timestamp(variableUpdate: VariableUpdate, zoneId: String): String? {
        return variableUpdate.timestamp.let { ResolverExtension.timestampToString(it, zoneId) }
    }

}
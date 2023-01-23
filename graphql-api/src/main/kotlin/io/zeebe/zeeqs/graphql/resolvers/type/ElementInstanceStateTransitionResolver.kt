package io.zeebe.zeeqs.graphql.resolvers.type

import io.zeebe.zeeqs.data.entity.ElementInstanceStateTransition
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class ElementInstanceStateTransitionResolver {

    @SchemaMapping(typeName = "ElementInstanceStateTransition", field = "timestamp")
    fun timestamp(
            elementInstanceStateTransition: ElementInstanceStateTransition,
            @Argument zoneId: String
    ): String? {
        return elementInstanceStateTransition.timestamp.let { ResolverExtension.timestampToString(it, zoneId) }
    }

}
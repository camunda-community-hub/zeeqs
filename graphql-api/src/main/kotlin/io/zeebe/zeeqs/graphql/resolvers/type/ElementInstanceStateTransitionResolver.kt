package io.zeebe.zeeqs.data.resolvers

import graphql.kickstart.tools.GraphQLResolver
import io.zeebe.zeeqs.data.entity.ElementInstanceStateTransition
import io.zeebe.zeeqs.graphql.resolvers.type.ResolverExtension
import org.springframework.stereotype.Component

@Component
class ElementInstanceStateTransitionResolver : GraphQLResolver<ElementInstanceStateTransition> {

    fun timestamp(elementInstanceStateTransition: ElementInstanceStateTransition, zoneId: String): String? {
        return elementInstanceStateTransition.timestamp.let { ResolverExtension.timestampToString(it, zoneId) }
    }

}
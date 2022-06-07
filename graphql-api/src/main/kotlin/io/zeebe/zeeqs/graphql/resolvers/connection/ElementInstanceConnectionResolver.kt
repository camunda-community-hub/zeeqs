package io.zeebe.zeeqs.graphql.resolvers.connection

import graphql.kickstart.tools.GraphQLResolver
import io.zeebe.zeeqs.data.entity.ElementInstance
import org.springframework.stereotype.Component

@Component
class ElementInstanceConnectionResolver : GraphQLResolver<ElementInstanceConnection> {

    fun nodes(connection: ElementInstanceConnection): List<ElementInstance> {
        return connection.getItems()
    }

    fun totalCount(connection: ElementInstanceConnection): Long {
        return connection.getCount()
    }

}
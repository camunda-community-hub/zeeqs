package io.zeebe.zeeqs.graphql.resolvers.connection

import io.zeebe.zeeqs.data.entity.ElementInstance
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class ElementInstanceConnectionResolver {

    @SchemaMapping(typeName = "ElementInstanceConnection", field = "nodes")
    fun nodes(connection: ElementInstanceConnection): List<ElementInstance> {
        return connection.getItems()
    }

    @SchemaMapping(typeName = "ElementInstanceConnection", field = "totalCount")
    fun totalCount(connection: ElementInstanceConnection): Long {
        return connection.getCount()
    }

}
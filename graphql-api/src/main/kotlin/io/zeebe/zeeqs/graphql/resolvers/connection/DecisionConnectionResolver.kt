package io.zeebe.zeeqs.graphql.resolvers.connection

import io.zeebe.zeeqs.data.entity.Decision
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class DecisionConnectionResolver {

    @SchemaMapping(typeName = "DecisionConnection", field = "nodes")
    fun nodes(connection: DecisionConnection): List<Decision> {
        return connection.getItems()
    }

    @SchemaMapping(typeName = "DecisionConnection", field = "totalCount")
    fun totalCount(connection: DecisionConnection): Long {
        return connection.getCount()
    }
}
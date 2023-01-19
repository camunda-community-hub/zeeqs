package io.zeebe.zeeqs.graphql.resolvers.connection

import io.zeebe.zeeqs.data.entity.Error
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class ErrorConnectionResolver {

    @SchemaMapping(typeName = "ErrorConnection", field = "nodes")
    fun nodes(connection: ErrorConnection): List<Error> {
        return connection.getItems()
    }

    @SchemaMapping(typeName = "ErrorConnection", field = "totalCount")
    fun totalCount(connection: ErrorConnection): Long {
        return connection.getCount()
    }

}
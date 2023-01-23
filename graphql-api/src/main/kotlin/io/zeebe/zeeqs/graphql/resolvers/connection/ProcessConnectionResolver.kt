package io.zeebe.zeeqs.graphql.resolvers.connection

import io.zeebe.zeeqs.data.entity.Process
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class ProcessConnectionResolver {

    @SchemaMapping(typeName = "ProcessConnection", field = "nodes")
    fun nodes(connection: ProcessConnection): List<Process> {
        return connection.getItems()
    }

    @SchemaMapping(typeName = "ProcessConnection", field = "totalCount")
    fun totalCount(connection: ProcessConnection): Long {
        return connection.getCount()
    }

}
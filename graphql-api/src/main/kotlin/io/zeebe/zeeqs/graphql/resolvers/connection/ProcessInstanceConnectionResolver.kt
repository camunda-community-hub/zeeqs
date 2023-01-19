package io.zeebe.zeeqs.graphql.resolvers.connection

import io.zeebe.zeeqs.data.entity.ProcessInstance
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class ProcessInstanceConnectionResolver {

    @SchemaMapping(typeName = "ProcessInstanceConnection", field = "nodes")
    fun nodes(connection: ProcessInstanceConnection): List<ProcessInstance> {
        return connection.getItems()
    }

    @SchemaMapping(typeName = "ProcessInstanceConnection", field = "totalCount")
    fun totalCount(connection: ProcessInstanceConnection): Long {
        return connection.getCount()
    }

}
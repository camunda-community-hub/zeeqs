package io.zeebe.zeeqs.graphql.resolvers.connection

import io.zeebe.zeeqs.data.entity.Job
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class JobConnectionResolver {

    @SchemaMapping(typeName = "JobConnection", field = "nodes")
    fun nodes(connection: JobConnection): List<Job> {
        return connection.getItems()
    }

    @SchemaMapping(typeName = "JobConnection", field = "totalCount")
    fun totalCount(connection: JobConnection): Long {
        return connection.getCount()
    }

}
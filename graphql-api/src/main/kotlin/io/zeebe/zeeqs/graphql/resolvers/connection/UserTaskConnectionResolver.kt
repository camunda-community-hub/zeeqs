package io.zeebe.zeeqs.graphql.resolvers.connection

import io.zeebe.zeeqs.data.entity.UserTask
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class UserTaskConnectionResolver {

    @SchemaMapping(typeName = "UserTaskConnection", field = "nodes")
    fun nodes(connection: UserTaskConnection): List<UserTask> {
        return connection.getItems()
    }

    @SchemaMapping(typeName = "UserTaskConnection", field = "totalCount")
    fun totalCount(connection: UserTaskConnection): Long {
        return connection.getCount()
    }

}
package io.zeebe.zeeqs.graphql.resolvers.connection

import io.zeebe.zeeqs.data.entity.Message
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class MessageConnectionResolver {

    @SchemaMapping(typeName = "MessageConnection", field = "nodes")
    fun nodes(connection: MessageConnection): List<Message> {
        return connection.getItems()
    }

    @SchemaMapping(typeName = "MessageConnection", field = "totalCount")
    fun totalCount(connection: MessageConnection): Long {
        return connection.getCount()
    }

}
package io.zeebe.zeeqs.graphql.resolvers.connection

import com.coxautodev.graphql.tools.GraphQLResolver
import io.zeebe.zeeqs.data.entity.Message
import org.springframework.stereotype.Component

@Component
class MessageConnectionResolver : GraphQLResolver<MessageConnection> {

    fun nodes(connection: MessageConnection): List<Message> {
        return connection.getItems()
    }

    fun totalCount(connection: MessageConnection): Long {
        return connection.getCount()
    }

}
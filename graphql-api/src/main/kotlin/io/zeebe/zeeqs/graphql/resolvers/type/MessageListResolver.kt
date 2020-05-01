package io.zeebe.zeeqs.graphql.resolvers.type

import com.coxautodev.graphql.tools.GraphQLResolver
import io.zeebe.zeeqs.data.entity.Message
import org.springframework.stereotype.Component

@Component
class MessageListResolver : GraphQLResolver<MessageList> {

    fun items(list: MessageList): List<Message> {
        return list.getItems()
    }

    fun totalCount(list: MessageList): Long {
        return list.getCount()
    }

}
package io.zeebe.zeeqs.data.resolvers

import com.coxautodev.graphql.tools.GraphQLQueryResolver
import io.zeebe.zeeqs.data.entity.Message
import io.zeebe.zeeqs.data.repository.MessageRepository
import io.zeebe.zeeqs.graphql.resolvers.connection.MessageConnection
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class MessageQueryResolver(
        val messageRepository: MessageRepository
) : GraphQLQueryResolver {

    fun messages(limit: Int, page: Int): MessageConnection {
        return MessageConnection(
                getItems = { messageRepository.findAll(PageRequest.of(page, limit)).toList() },
                getCount = { messageRepository.count() }
        )
    }

    fun message(key: Long): Message? {
        return messageRepository.findByIdOrNull(key)
    }

}
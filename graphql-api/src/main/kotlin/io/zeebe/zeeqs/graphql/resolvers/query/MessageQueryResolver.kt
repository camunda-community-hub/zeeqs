package io.zeebe.zeeqs.data.resolvers

import com.coxautodev.graphql.tools.GraphQLQueryResolver
import io.zeebe.zeeqs.data.entity.Message
import io.zeebe.zeeqs.data.repository.MessageRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class MessageQueryResolver(
        val messageRepository: MessageRepository
) : GraphQLQueryResolver {

    fun messages(count: Int, offset: Int): List<Message> {
        return messageRepository.findAll(PageRequest.of(offset, count)).toList()
    }

    fun message(key: Long): Message? {
        return messageRepository.findByIdOrNull(key)
    }

}
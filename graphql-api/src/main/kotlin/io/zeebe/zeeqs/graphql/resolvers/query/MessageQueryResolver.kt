package io.zeebe.zeeqs.data.resolvers

import graphql.kickstart.tools.GraphQLQueryResolver
import io.zeebe.zeeqs.data.entity.Message
import io.zeebe.zeeqs.data.entity.MessageState
import io.zeebe.zeeqs.data.repository.MessageRepository
import io.zeebe.zeeqs.graphql.resolvers.connection.MessageConnection
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class MessageQueryResolver(
        val messageRepository: MessageRepository
) : GraphQLQueryResolver {

    fun messages(perPage: Int, page: Int, stateIn: List<MessageState>): MessageConnection {
        return MessageConnection(
                getItems = {
                    messageRepository.findByStateIn(
                            stateIn = stateIn,
                            pageable = PageRequest.of(page, perPage)
                    )
                },
                getCount = {
                    messageRepository.countByStateIn(
                            stateIn = stateIn
                    )
                }
        )
    }

    fun message(key: Long): Message? {
        return messageRepository.findByIdOrNull(key)
    }

}
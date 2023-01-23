package io.zeebe.zeeqs.graphql.resolvers.query

import io.zeebe.zeeqs.data.entity.Message
import io.zeebe.zeeqs.data.entity.MessageState
import io.zeebe.zeeqs.data.repository.MessageRepository
import io.zeebe.zeeqs.graphql.resolvers.connection.MessageConnection
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class MessageQueryResolver(
        val messageRepository: MessageRepository
) {

    @QueryMapping
    fun messages(
            @Argument perPage: Int,
            @Argument page: Int,
            @Argument stateIn: List<MessageState>
    ): MessageConnection {
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

    @QueryMapping
    fun message(@Argument key: Long): Message? {
        return messageRepository.findByIdOrNull(key)
    }

}
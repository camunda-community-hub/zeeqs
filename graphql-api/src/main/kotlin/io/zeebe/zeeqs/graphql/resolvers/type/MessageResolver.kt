package io.zeebe.zeeqs.data.resolvers

import com.coxautodev.graphql.tools.GraphQLResolver
import io.zeebe.zeeqs.data.entity.Message
import io.zeebe.zeeqs.data.entity.MessageCorrelation
import io.zeebe.zeeqs.data.repository.MessageCorrelationRepository
import org.springframework.stereotype.Component

@Component
class MessageResolver(
        val messageCorrelationRepository: MessageCorrelationRepository
) : GraphQLResolver<Message> {

    fun messageCorrelations(message: Message): List<MessageCorrelation> {
        return messageCorrelationRepository.findByMessageKey(message.key)
    }

}
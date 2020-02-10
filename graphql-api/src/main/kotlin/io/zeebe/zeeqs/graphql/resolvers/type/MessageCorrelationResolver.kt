package io.zeebe.zeeqs.data.resolvers

import com.coxautodev.graphql.tools.GraphQLResolver
import io.zeebe.zeeqs.data.entity.Message
import io.zeebe.zeeqs.data.entity.MessageCorrelation
import io.zeebe.zeeqs.data.entity.MessageSubscription
import io.zeebe.zeeqs.data.repository.MessageRepository
import io.zeebe.zeeqs.data.repository.MessageSubscriptionRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class MessageCorrelationResolver(
        val messageSubscriptionRepository: MessageSubscriptionRepository,
        val messageRepository: MessageRepository
) : GraphQLResolver<MessageCorrelation> {

    fun messageSubscription(messageCorrelation: MessageCorrelation): MessageSubscription? {
        return messageSubscriptionRepository.findByElementInstanceKeyAndMessageName(
                elementInstanceKey = messageCorrelation.elementInstanceKey,
                messageName = messageCorrelation.messageName
        )
    }

    fun message(messageCorrelation: MessageCorrelation): Message? {
        return messageRepository.findByIdOrNull(messageCorrelation.messageKey)
    }
}
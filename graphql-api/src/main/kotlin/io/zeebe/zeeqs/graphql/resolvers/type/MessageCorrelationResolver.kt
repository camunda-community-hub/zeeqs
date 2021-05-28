package io.zeebe.zeeqs.data.resolvers

import graphql.kickstart.tools.GraphQLResolver
import io.zeebe.zeeqs.data.entity.Message
import io.zeebe.zeeqs.data.entity.MessageCorrelation
import io.zeebe.zeeqs.data.entity.MessageSubscription
import io.zeebe.zeeqs.data.repository.MessageRepository
import io.zeebe.zeeqs.data.repository.MessageSubscriptionRepository
import io.zeebe.zeeqs.graphql.resolvers.type.ResolverExtension
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class MessageCorrelationResolver(
    val messageSubscriptionRepository: MessageSubscriptionRepository,
    val messageRepository: MessageRepository
) : GraphQLResolver<MessageCorrelation> {

    fun timestamp(messageCorrelation: MessageCorrelation, zoneId: String): String? {
        return messageCorrelation.timestamp.let { ResolverExtension.timestampToString(it, zoneId) }
    }

    fun messageSubscription(messageCorrelation: MessageCorrelation): MessageSubscription? {
        return messageCorrelation.elementInstanceKey
            ?.let {
                messageSubscriptionRepository.findByElementInstanceKeyAndMessageName(
                    elementInstanceKey = it,
                    messageName = messageCorrelation.messageName
                )
            }
            ?: messageCorrelation.processDefinitionKey?.let {
                messageSubscriptionRepository.findByProcessDefinitionKeyAndMessageName(
                    processDefinitionKey = it,
                    messageName = messageCorrelation.messageName
                )
            }
    }

    fun message(messageCorrelation: MessageCorrelation): Message? {
        return messageRepository.findByIdOrNull(messageCorrelation.messageKey)
    }
}
package io.zeebe.zeeqs.data.resolvers

import graphql.kickstart.tools.GraphQLResolver
import io.zeebe.zeeqs.data.entity.Message
import io.zeebe.zeeqs.data.entity.MessageCorrelation
import io.zeebe.zeeqs.data.entity.MessageSubscription
import io.zeebe.zeeqs.data.entity.MessageVariable
import io.zeebe.zeeqs.data.repository.MessageCorrelationRepository
import io.zeebe.zeeqs.data.repository.MessageVariableRepository
import io.zeebe.zeeqs.graphql.resolvers.type.ResolverExtension
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class MessageResolver(
        val messageCorrelationRepository: MessageCorrelationRepository,
        val messageVariableRepository: MessageVariableRepository
) : GraphQLResolver<Message> {

    fun timestamp(message: Message, zoneId: String): String? {
        return message.timestamp.let { ResolverExtension.timestampToString(it, zoneId) }
    }

    fun timeToLive(message: Message): String? {
        return message.timeToLive.let { Duration.ofMillis(it).toString() }
    }

    fun messageCorrelations(message: Message): List<MessageCorrelation> {
        return messageCorrelationRepository.findByMessageKey(message.key)
    }

    fun variables(message: Message): List<MessageVariable> {
        return messageVariableRepository.findByMessageKey(messageKey = message.key)
    }

}
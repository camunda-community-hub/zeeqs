package io.zeebe.zeeqs.graphql.resolvers.type

import io.zeebe.zeeqs.data.entity.Message
import io.zeebe.zeeqs.data.entity.MessageCorrelation
import io.zeebe.zeeqs.data.entity.MessageVariable
import io.zeebe.zeeqs.data.repository.MessageCorrelationRepository
import io.zeebe.zeeqs.data.repository.MessageVariableRepository
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller
import java.time.Duration

@Controller
class MessageResolver(
        val messageCorrelationRepository: MessageCorrelationRepository,
        val messageVariableRepository: MessageVariableRepository
) {

    @SchemaMapping(typeName = "Message", field = "timestamp")
    fun timestamp(
            message: Message,
            @Argument zoneId: String
    ): String? {
        return message.timestamp.let { ResolverExtension.timestampToString(it, zoneId) }
    }

    @SchemaMapping(typeName = "Message", field = "timeToLive")
    fun timeToLive(message: Message): String? {
        return message.timeToLive.let { Duration.ofMillis(it).toString() }
    }

    @SchemaMapping(typeName = "Message", field = "messageCorrelations")
    fun messageCorrelations(message: Message): List<MessageCorrelation> {
        return messageCorrelationRepository.findByMessageKey(message.key)
    }

    @SchemaMapping(typeName = "Message", field = "variables")
    fun variables(message: Message): List<MessageVariable> {
        return messageVariableRepository.findByMessageKey(messageKey = message.key)
    }

}
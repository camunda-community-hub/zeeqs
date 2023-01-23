package io.zeebe.zeeqs.graphql.resolvers.type

import io.zeebe.zeeqs.data.entity.Message
import io.zeebe.zeeqs.data.entity.MessageCorrelation
import io.zeebe.zeeqs.data.entity.MessageSubscription
import io.zeebe.zeeqs.data.entity.ProcessInstance
import io.zeebe.zeeqs.data.repository.MessageRepository
import io.zeebe.zeeqs.data.repository.MessageSubscriptionRepository
import io.zeebe.zeeqs.data.repository.ProcessInstanceRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class MessageCorrelationResolver(
        val messageSubscriptionRepository: MessageSubscriptionRepository,
        val messageRepository: MessageRepository,
        val processInstanceRepository: ProcessInstanceRepository
) {

    @SchemaMapping(typeName = "MessageCorrelation", field = "timestamp")
    fun timestamp(
            messageCorrelation: MessageCorrelation,
            @Argument zoneId: String
    ): String? {
        return messageCorrelation.timestamp.let { ResolverExtension.timestampToString(it, zoneId) }
    }

    @SchemaMapping(typeName = "MessageCorrelation", field = "messageSubscription")
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

    @SchemaMapping(typeName = "MessageCorrelation", field = "message")
    fun message(messageCorrelation: MessageCorrelation): Message? {
        return messageRepository.findByIdOrNull(messageCorrelation.messageKey)
    }

    @SchemaMapping(typeName = "MessageCorrelation", field = "processInstance")
    fun processInstance(messageCorrelation: MessageCorrelation): ProcessInstance? {
        return processInstanceRepository.findByIdOrNull(messageCorrelation.processInstanceKey)
    }
}
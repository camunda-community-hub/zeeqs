package io.zeebe.zeeqs.graphql.resolvers.type

import io.zeebe.zeeqs.data.entity.*
import io.zeebe.zeeqs.data.repository.ElementInstanceRepository
import io.zeebe.zeeqs.data.repository.MessageCorrelationRepository
import io.zeebe.zeeqs.data.repository.ProcessInstanceRepository
import io.zeebe.zeeqs.data.repository.ProcessRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class MessageSubscriptionResolver(
        val processInstanceRepository: ProcessInstanceRepository,
        val elementInstanceRepository: ElementInstanceRepository,
        val processRepository: ProcessRepository,
        val messageCorrelationRepository: MessageCorrelationRepository
) {

    @SchemaMapping(typeName = "MessageSubscription", field = "timestamp")
    fun timestamp(
            messageSubscription: MessageSubscription,
            @Argument zoneId: String
    ): String? {
        return messageSubscription.timestamp.let { ResolverExtension.timestampToString(it, zoneId) }
    }

    @SchemaMapping(typeName = "MessageSubscription", field = "processInstance")
    fun processInstance(messageSubscription: MessageSubscription): ProcessInstance? {
        return messageSubscription.processInstanceKey?.let { processInstanceRepository.findByIdOrNull(it) }
    }

    @SchemaMapping(typeName = "MessageSubscription", field = "elementInstance")
    fun elementInstance(messageSubscription: MessageSubscription): ElementInstance? {
        return messageSubscription.elementInstanceKey?.let { elementInstanceRepository.findByIdOrNull(it) }
    }

    @SchemaMapping(typeName = "MessageSubscription", field = "process")
    fun process(messageSubscription: MessageSubscription): Process? {
        return messageSubscription.processDefinitionKey?.let { processRepository.findByIdOrNull(it) }
                ?: messageSubscription.processInstanceKey?.let {
                    processInstanceRepository.findByIdOrNull(it)
                            ?.processDefinitionKey.let { processRepository.findByIdOrNull(it) }
                }
    }

    @SchemaMapping(typeName = "MessageSubscription", field = "messageCorrelations")
    fun messageCorrelations(messageSubscription: MessageSubscription): List<MessageCorrelation> {
        return messageSubscription.elementInstanceKey
                ?.let {
                    messageCorrelationRepository.findByMessageNameAndElementInstanceKey(
                            messageName = messageSubscription.messageName,
                            elementInstanceKey = it)
                }
                ?: messageSubscription.processDefinitionKey?.let {
                    messageCorrelationRepository.findByMessageNameAndProcessDefinitionKey(
                            messageName = messageSubscription.messageName,
                            processDefinitionKey = it
                    )
                }
                ?: emptyList()
    }

    @SchemaMapping(typeName = "MessageSubscription", field = "element")
    fun element(messageSubscription: MessageSubscription): BpmnElement? {
        val processDefinitionKeyOfSubscription = messageSubscription.processDefinitionKey
                ?: processInstanceRepository
                        .findByIdOrNull(messageSubscription.processInstanceKey)
                        ?.processDefinitionKey

        return processDefinitionKeyOfSubscription?.let { processDefinitionKey ->
            messageSubscription.elementId?.let { elementId ->
                BpmnElement(
                        processDefinitionKey = processDefinitionKey,
                        elementId = elementId
                )
            }
        }
    }

}
package io.zeebe.zeeqs.data.resolvers

import graphql.kickstart.tools.GraphQLResolver
import io.zeebe.zeeqs.data.entity.*
import io.zeebe.zeeqs.data.repository.ElementInstanceRepository
import io.zeebe.zeeqs.data.repository.MessageCorrelationRepository
import io.zeebe.zeeqs.data.repository.WorkflowInstanceRepository
import io.zeebe.zeeqs.data.repository.ProcessRepository
import io.zeebe.zeeqs.graphql.resolvers.type.ResolverExtension
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class MessageSubscriptionResolver(
    val workflowInstanceRepository: WorkflowInstanceRepository,
    val elementInstanceRepository: ElementInstanceRepository,
    val processRepository: ProcessRepository,
    val messageCorrelationRepository: MessageCorrelationRepository
) : GraphQLResolver<MessageSubscription> {

    fun timestamp(messageSubscription: MessageSubscription, zoneId: String): String? {
        return messageSubscription.timestamp.let { ResolverExtension.timestampToString(it, zoneId) }
    }

    fun workflowInstance(messageSubscription: MessageSubscription): WorkflowInstance? {
        return messageSubscription.processInstanceKey?.let { workflowInstanceRepository.findByIdOrNull(it) }
    }

    fun elementInstance(messageSubscription: MessageSubscription): ElementInstance? {
        return messageSubscription.elementInstanceKey?.let { elementInstanceRepository.findByIdOrNull(it) }
    }

    fun workflow(messageSubscription: MessageSubscription): Process? {
        return messageSubscription.processDefinitionKey?.let { processRepository.findByIdOrNull(it) }
    }

    fun messageCorrelations(messageSubscription: MessageSubscription): List<MessageCorrelation> {
        return messageSubscription.elementInstanceKey
                ?.let {
                    messageCorrelationRepository.findByMessageNameAndElementInstanceKey(
                            messageName = messageSubscription.messageName,
                            elementInstanceKey = it)
                }
                ?: emptyList()
    }

}
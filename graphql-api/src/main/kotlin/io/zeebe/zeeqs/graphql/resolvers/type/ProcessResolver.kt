package io.zeebe.zeeqs.graphql.resolvers.type

import graphql.kickstart.tools.GraphQLResolver
import io.zeebe.zeeqs.data.entity.*
import io.zeebe.zeeqs.data.repository.MessageSubscriptionRepository
import io.zeebe.zeeqs.data.repository.TimerRepository
import io.zeebe.zeeqs.data.repository.ProcessInstanceRepository
import io.zeebe.zeeqs.data.service.BpmnElementInfo
import io.zeebe.zeeqs.data.service.ProcessService
import io.zeebe.zeeqs.graphql.resolvers.connection.ProcessInstanceConnection
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component

@Component
class ProcessResolver(
        val processInstanceRepository: ProcessInstanceRepository,
        val timerRepository: TimerRepository,
        val messageSubscriptionRepository: MessageSubscriptionRepository,
        val processService: ProcessService
) : GraphQLResolver<Process> {

    fun processInstances(process: Process, perPage: Int, page: Int, stateIn: List<ProcessInstanceState>): ProcessInstanceConnection {
        return ProcessInstanceConnection(
                getItems = { processInstanceRepository.findByProcessDefinitionKeyAndStateIn(process.key, stateIn, PageRequest.of(page, perPage)).toList() },
                getCount = { processInstanceRepository.countByProcessDefinitionKeyAndStateIn(process.key, stateIn) }
        )
    }

    fun deployTime(process: Process, zoneId: String): String? {
        return process.deployTime.let { ResolverExtension.timestampToString(it, zoneId) }
    }

    fun timers(process: Process): List<Timer> {
        return timerRepository.findByProcessDefinitionKeyAndElementInstanceKeyIsNull(process.key)
    }

    fun messageSubscriptions(process: Process): List<MessageSubscription> {
        return messageSubscriptionRepository.findByProcessDefinitionKeyAndElementInstanceKeyIsNull(process.key)
    }

    fun elements(process: Process, elementTypeIn: List<BpmnElementType>): List<BpmnElement> {
        return processService
                .getBpmnElementInfo(process.key)
                ?.values
                ?.filter { elementTypeIn.isEmpty() || elementTypeIn.contains(it.elementType) }
                ?.map { asBpmnElement(process, it) }
                ?: emptyList()
    }

    private fun asBpmnElement(process: Process, it: BpmnElementInfo) =
            BpmnElement(
                    processDefinitionKey = process.key,
                    elementId = it.elementId,
                    elementType = it.elementType
            )

    fun element(process: Process, elementId: String): BpmnElement? {
        return processService
                .getBpmnElementInfo(process.key)
                ?.get(elementId)
                ?.let { asBpmnElement(process, it) }
    }

}
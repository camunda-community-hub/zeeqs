package io.zeebe.zeeqs.graphql.resolvers.type

import io.zeebe.zeeqs.data.entity.*
import io.zeebe.zeeqs.data.repository.MessageSubscriptionRepository
import io.zeebe.zeeqs.data.repository.ProcessInstanceRepository
import io.zeebe.zeeqs.data.repository.TimerRepository
import io.zeebe.zeeqs.data.service.BpmnElementInfo
import io.zeebe.zeeqs.data.service.ProcessService
import io.zeebe.zeeqs.graphql.resolvers.connection.ProcessInstanceConnection
import org.springframework.data.domain.PageRequest
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class ProcessResolver(
        val processInstanceRepository: ProcessInstanceRepository,
        val timerRepository: TimerRepository,
        val messageSubscriptionRepository: MessageSubscriptionRepository,
        val processService: ProcessService
) {

    @SchemaMapping(typeName = "Process", field = "processInstances")
    fun processInstances(
            process: Process,
            @Argument perPage: Int,
            @Argument page: Int,
            @Argument stateIn: List<ProcessInstanceState>
    ): ProcessInstanceConnection {
        return ProcessInstanceConnection(
                getItems = { processInstanceRepository.findByProcessDefinitionKeyAndStateIn(process.key, stateIn, PageRequest.of(page, perPage)).toList() },
                getCount = { processInstanceRepository.countByProcessDefinitionKeyAndStateIn(process.key, stateIn) }
        )
    }

    @SchemaMapping(typeName = "Process", field = "deployTime")
    fun deployTime(
            process: Process,
            @Argument zoneId: String
    ): String? {
        return process.deployTime.let { ResolverExtension.timestampToString(it, zoneId) }
    }

    @SchemaMapping(typeName = "Process", field = "timers")
    fun timers(process: Process): List<Timer> {
        return timerRepository.findByProcessDefinitionKeyAndElementInstanceKeyIsNull(process.key)
    }

    @SchemaMapping(typeName = "Process", field = "messageSubscriptions")
    fun messageSubscriptions(process: Process): List<MessageSubscription> {
        return messageSubscriptionRepository.findByProcessDefinitionKeyAndElementInstanceKeyIsNull(process.key)
    }

    @SchemaMapping(typeName = "Process", field = "elements")
    fun elements(
            process: Process,
            @Argument elementTypeIn: List<BpmnElementType>
    ): List<BpmnElement> {
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

    @SchemaMapping(typeName = "Process", field = "element")
    fun element(
            process: Process,
            @Argument elementId: String
    ): BpmnElement? {
        return processService
                .getBpmnElementInfo(process.key)
                ?.get(elementId)
                ?.let { asBpmnElement(process, it) }
    }

}
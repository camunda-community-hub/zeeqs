package io.zeebe.zeeqs.data.resolvers

import graphql.kickstart.tools.GraphQLResolver
import io.zeebe.zeeqs.data.entity.*
import io.zeebe.zeeqs.data.repository.ElementInstanceRepository
import io.zeebe.zeeqs.data.repository.ProcessRepository
import io.zeebe.zeeqs.data.service.BpmnElementInfo
import io.zeebe.zeeqs.data.service.BpmnElementMetadata
import io.zeebe.zeeqs.data.service.MessageSubscriptionDefinition
import io.zeebe.zeeqs.graphql.resolvers.type.BpmnElement
import io.zeebe.zeeqs.data.service.ProcessService
import io.zeebe.zeeqs.graphql.resolvers.connection.ElementInstanceConnection
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class BpmnElementResolver(
        val processRepository: ProcessRepository,
        val processService: ProcessService,
        val elementInstanceRepository: ElementInstanceRepository
) : GraphQLResolver<BpmnElement> {

    fun elementName(element: BpmnElement): String? {
        return findElementInfo(element)?.elementName
    }

    fun bpmnElementType(element: BpmnElement): BpmnElementType {
        return element.elementType
                ?: findElementInfo(element)?.elementType
                ?: BpmnElementType.UNKNOWN
    }

    fun metadata(element: BpmnElement): BpmnElementMetadata? {
        return findElementInfo(element)?.metadata
    }

    fun process(element: BpmnElement): Process? {
        return processRepository.findByIdOrNull(element.processDefinitionKey)
    }

    private fun findElementInfo(element: BpmnElement): BpmnElementInfo? {
        return processService
                .getBpmnElementInfo(element.processDefinitionKey)
                ?.get(element.elementId)
    }

    fun elementInstances(
            element: BpmnElement,
            perPage: Int,
            page: Int,
            stateIn: List<ElementInstanceState>
    ): ElementInstanceConnection {
        return ElementInstanceConnection(
                getItems = {
                    elementInstanceRepository.findByProcessDefinitionKeyAndElementIdAndStateIn(
                            processDefinitionKey = element.processDefinitionKey,
                            elementId = element.elementId,
                            stateIn = stateIn,
                            pageable = PageRequest.of(page, perPage)
                    )
                },
                getCount = {
                    elementInstanceRepository.countByProcessDefinitionKeyAndElementIdAndStateIn(
                            processDefinitionKey = element.processDefinitionKey,
                            elementId = element.elementId,
                            stateIn = stateIn
                    )
                }
        )
    }

}
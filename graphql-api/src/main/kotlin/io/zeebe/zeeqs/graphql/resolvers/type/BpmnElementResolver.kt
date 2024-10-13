package io.zeebe.zeeqs.graphql.resolvers.type

import io.zeebe.zeeqs.data.entity.BpmnElementType
import io.zeebe.zeeqs.data.entity.ElementInstanceState
import io.zeebe.zeeqs.data.entity.Process
import io.zeebe.zeeqs.data.repository.ElementInstanceRepository
import io.zeebe.zeeqs.data.repository.ProcessRepository
import io.zeebe.zeeqs.data.service.*
import io.zeebe.zeeqs.graphql.resolvers.connection.ElementInstanceConnection
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class BpmnElementResolver(
        val processRepository: ProcessRepository,
        val processService: ProcessService,
        val elementInstanceRepository: ElementInstanceRepository
) {

    @SchemaMapping(typeName = "BpmnElement", field = "elementName")
    fun elementName(element: BpmnElement): String? {
        return findElementInfo(element)?.elementName
    }

    @SchemaMapping(typeName = "BpmnElement", field = "bpmnElementType")
    fun bpmnElementType(element: BpmnElement): BpmnElementType {
        return element.elementType
                ?: findElementInfo(element)?.elementType
                ?: BpmnElementType.UNKNOWN
    }

    @SchemaMapping(typeName = "BpmnElement", field = "metadata")
    fun metadata(element: BpmnElement): BpmnElementMetadata {
        return findElementInfo(element)
                ?.metadata
                ?: BpmnElementMetadata()
    }


    @SchemaMapping(typeName = "BpmnElement", field = "extensionElements")
    fun extensionElements(element: BpmnElement): BpmnExtensionElements? {
        return findElementInfo(element)
                ?.extensionElements
    }


    @SchemaMapping(typeName = "BpmnElement", field = "documentation")
    fun documentation(element: BpmnElement): String? {
        return findElementInfo(element)
                ?.documentation

    }

    @SchemaMapping(typeName = "BpmnElement", field = "process")
    fun process(element: BpmnElement): Process? {
        return processRepository.findByIdOrNull(element.processDefinitionKey)
    }

    private fun findElementInfo(element: BpmnElement): BpmnElementInfo? {
        return processService
                .getBpmnElementInfo(element.processDefinitionKey)
                ?.get(element.elementId)
    }

    @SchemaMapping(typeName = "BpmnElement", field = "elementInstances")
    fun elementInstances(
            element: BpmnElement,
            @Argument perPage: Int,
            @Argument page: Int,
            @Argument stateIn: List<ElementInstanceState>
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
package io.zeebe.zeeqs.data.resolvers

import graphql.kickstart.tools.GraphQLResolver
import io.zeebe.zeeqs.data.entity.BpmnElementType
import io.zeebe.zeeqs.data.entity.Process
import io.zeebe.zeeqs.data.repository.ProcessRepository
import io.zeebe.zeeqs.data.service.BpmnElementInfo
import io.zeebe.zeeqs.graphql.resolvers.type.BpmnElement
import io.zeebe.zeeqs.data.service.ProcessService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class BpmnElementResolver(
        val processRepository: ProcessRepository,
        val processService: ProcessService
) : GraphQLResolver<BpmnElement> {

    fun elementName(element: BpmnElement): String? {
        return findElementInfo(element)?.elementName
    }

    fun bpmnElementType(element: BpmnElement): BpmnElementType {
        return element.elementType
                ?: findElementInfo(element)?.elementType
                ?: BpmnElementType.UNKNOWN
    }

    fun process(element: BpmnElement): Process? {
        return processRepository.findByIdOrNull(element.processDefinitionKey)
    }

    private fun findElementInfo(element: BpmnElement): BpmnElementInfo? {
        return processService
                .getBpmnElementInfo(element.processDefinitionKey)
                ?.get(element.elementId)
    }

}
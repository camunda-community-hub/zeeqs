package io.zeebe.zeeqs.data.resolvers

import com.coxautodev.graphql.tools.GraphQLResolver
import io.zeebe.zeeqs.data.entity.ElementInstance
import io.zeebe.zeeqs.data.entity.ElementInstanceStateTransition
import io.zeebe.zeeqs.data.entity.Incident
import io.zeebe.zeeqs.data.entity.WorkflowInstance
import io.zeebe.zeeqs.data.repository.ElementInstanceRepository
import io.zeebe.zeeqs.data.repository.ElementInstanceStateTransitionRepository
import io.zeebe.zeeqs.data.repository.IncidentRepository
import io.zeebe.zeeqs.data.repository.WorkflowInstanceRepository
import io.zeebe.zeeqs.data.service.WorkflowService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class ElementInstanceResolver(
        val elementInstanceRepository: ElementInstanceRepository,
        val workflowInstanceRepository: WorkflowInstanceRepository,
        val incidentRepository: IncidentRepository,
        val elementInstanceStateTransitionRepository: ElementInstanceStateTransitionRepository,
        val workflowService: WorkflowService
) : GraphQLResolver<ElementInstance> {

    fun workflowInstance(elementInstance: ElementInstance): WorkflowInstance? {
        return workflowInstanceRepository.findByIdOrNull(elementInstance.workflowInstanceKey)
    }

    fun incidents(elementInstance: ElementInstance): List<Incident> {
        return incidentRepository.findByJobKey(elementInstance.key)
    }

    fun scope(elementInstance: ElementInstance): ElementInstance? {
        return elementInstance.scopeKey?.let { elementInstanceRepository.findByIdOrNull(it) }
    }

    fun stateTransitions(elementInstance: ElementInstance): List<ElementInstanceStateTransition> {
        return elementInstanceStateTransitionRepository.findByElementInstanceKey(elementInstance.key)
    }

    fun elementName(elementInstance: ElementInstance): String? {
        return workflowService
                .getBpmnElementInfo(elementInstance.workflowKey)
                ?.get(elementInstance.elementId)
                ?.elementName
    }

}
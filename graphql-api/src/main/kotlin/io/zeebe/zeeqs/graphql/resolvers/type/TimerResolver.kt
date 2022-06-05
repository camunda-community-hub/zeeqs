package io.zeebe.zeeqs.graphql.resolvers.type

import graphql.kickstart.tools.GraphQLResolver
import io.zeebe.zeeqs.data.entity.ElementInstance
import io.zeebe.zeeqs.data.entity.Timer
import io.zeebe.zeeqs.data.entity.Process
import io.zeebe.zeeqs.data.entity.ProcessInstance
import io.zeebe.zeeqs.data.repository.ElementInstanceRepository
import io.zeebe.zeeqs.data.repository.ProcessInstanceRepository
import io.zeebe.zeeqs.data.repository.ProcessRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class TimerResolver(
    val processRepository: ProcessRepository,
    val processInstanceRepository: ProcessInstanceRepository,
    val elementInstanceRepository: ElementInstanceRepository
) : GraphQLResolver<Timer> {

    fun startTime(timer: Timer, zoneId: String): String? {
        return timer.startTime?.let { ResolverExtension.timestampToString(it, zoneId) }
    }

    fun endTime(timer: Timer, zoneId: String): String? {
        return timer.endTime?.let { ResolverExtension.timestampToString(it, zoneId) }
    }

    fun dueDate(timer: Timer, zoneId: String): String? {
        return timer.dueDate.let { ResolverExtension.timestampToString(it, zoneId) }
    }

    fun process(timer: Timer): Process? {
        return timer.processDefinitionKey?.let { processRepository.findByIdOrNull(it) }
    }

    fun processInstance(timer: Timer): ProcessInstance? {
        return timer.processInstanceKey?.let { processInstanceRepository.findByIdOrNull(it) }
    }

    fun elementInstance(timer: Timer): ElementInstance? {
        return timer.elementInstanceKey?.let { elementInstanceRepository.findByIdOrNull(it) }
    }

    fun element(timer: Timer): BpmnElement? {
        return timer.processDefinitionKey?.let {
            BpmnElement(
                    processDefinitionKey = it,
                    elementId = timer.elementId
            )
        }
    }

}
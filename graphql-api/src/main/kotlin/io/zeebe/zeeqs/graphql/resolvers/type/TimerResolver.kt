package io.zeebe.zeeqs.graphql.resolvers.type

import io.zeebe.zeeqs.data.entity.ElementInstance
import io.zeebe.zeeqs.data.entity.Process
import io.zeebe.zeeqs.data.entity.ProcessInstance
import io.zeebe.zeeqs.data.entity.Timer
import io.zeebe.zeeqs.data.repository.ElementInstanceRepository
import io.zeebe.zeeqs.data.repository.ProcessInstanceRepository
import io.zeebe.zeeqs.data.repository.ProcessRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class TimerResolver(
        val processRepository: ProcessRepository,
        val processInstanceRepository: ProcessInstanceRepository,
        val elementInstanceRepository: ElementInstanceRepository
) {

    @SchemaMapping(typeName = "Timer", field = "startTime")
    fun startTime(
            timer: Timer,
            @Argument zoneId: String
    ): String? {
        return timer.startTime?.let { ResolverExtension.timestampToString(it, zoneId) }
    }

    @SchemaMapping(typeName = "Timer", field = "endTime")
    fun endTime(
            timer: Timer,
            @Argument zoneId: String
    ): String? {
        return timer.endTime?.let { ResolverExtension.timestampToString(it, zoneId) }
    }

    @SchemaMapping(typeName = "Timer", field = "dueDate")
    fun dueDate(
            timer: Timer,
            @Argument zoneId: String
    ): String? {
        return timer.dueDate.let { ResolverExtension.timestampToString(it, zoneId) }
    }

    @SchemaMapping(typeName = "Timer", field = "process")
    fun process(timer: Timer): Process? {
        return timer.processDefinitionKey?.let { processRepository.findByIdOrNull(it) }
    }

    @SchemaMapping(typeName = "Timer", field = "processInstance")
    fun processInstance(timer: Timer): ProcessInstance? {
        return timer.processInstanceKey?.let { processInstanceRepository.findByIdOrNull(it) }
    }

    @SchemaMapping(typeName = "Timer", field = "elementInstance")
    fun elementInstance(timer: Timer): ElementInstance? {
        return timer.elementInstanceKey?.let { elementInstanceRepository.findByIdOrNull(it) }
    }

    @SchemaMapping(typeName = "Timer", field = "element")
    fun element(timer: Timer): BpmnElement? {
        return timer.processDefinitionKey?.let {
            BpmnElement(
                    processDefinitionKey = it,
                    elementId = timer.elementId
            )
        }
    }

}
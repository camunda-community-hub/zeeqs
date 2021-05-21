package io.zeebe.zeeqs.graphql.resolvers.type

import graphql.kickstart.tools.GraphQLResolver
import io.zeebe.zeeqs.data.entity.ElementInstance
import io.zeebe.zeeqs.data.entity.Timer
import io.zeebe.zeeqs.data.entity.Process
import io.zeebe.zeeqs.data.entity.ProcessIntance
import io.zeebe.zeeqs.data.repository.ElementInstanceRepository
import io.zeebe.zeeqs.data.repository.WorkflowInstanceRepository
import io.zeebe.zeeqs.data.repository.ProcessRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class TimerResolver(
    val processRepository: ProcessRepository,
    val workflowInstanceRepository: WorkflowInstanceRepository,
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

    fun workflow(timer: Timer): Process? {
        return timer.processDefinitionKey?.let { processRepository.findByIdOrNull(it) }
    }

    fun workflowInstance(timer: Timer): ProcessIntance? {
        return timer.processInstanceKey?.let { workflowInstanceRepository.findByIdOrNull(it) }
    }

    fun elementInstance(timer: Timer): ElementInstance? {
        return timer.elementInstanceKey?.let { elementInstanceRepository.findByIdOrNull(it) }
    }

}
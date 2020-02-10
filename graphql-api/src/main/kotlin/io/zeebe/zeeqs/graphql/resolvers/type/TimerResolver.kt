package io.zeebe.zeeqs.graphql.resolvers.type

import com.coxautodev.graphql.tools.GraphQLResolver
import io.zeebe.zeeqs.data.entity.ElementInstance
import io.zeebe.zeeqs.data.entity.Timer
import io.zeebe.zeeqs.data.entity.Workflow
import io.zeebe.zeeqs.data.entity.WorkflowInstance
import io.zeebe.zeeqs.data.repository.ElementInstanceRepository
import io.zeebe.zeeqs.data.repository.WorkflowInstanceRepository
import io.zeebe.zeeqs.data.repository.WorkflowRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class TimerResolver(
        val workflowRepository: WorkflowRepository,
        val workflowInstanceRepository: WorkflowInstanceRepository,
        val elementInstanceRepository: ElementInstanceRepository
) : GraphQLResolver<Timer> {

    fun workflow(timer: Timer): Workflow? {
        return timer.workflowKey?.let { workflowRepository.findByIdOrNull(it) }
    }

    fun workflowInstance(timer: Timer): WorkflowInstance? {
        return timer.workflowInstanceKey?.let { workflowInstanceRepository.findByIdOrNull(it) }
    }

    fun elementInstance(timer: Timer): ElementInstance? {
        return timer.elementInstanceKey?.let { elementInstanceRepository.findByIdOrNull(it) }
    }

}
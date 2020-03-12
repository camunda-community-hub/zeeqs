package io.zeebe.zeeqs.data.resolvers

import com.coxautodev.graphql.tools.GraphQLQueryResolver
import io.zeebe.zeeqs.data.entity.WorkflowInstance
import io.zeebe.zeeqs.data.entity.WorkflowInstanceState
import io.zeebe.zeeqs.data.repository.WorkflowInstanceRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class WorkflowInstanceQueryResolver(
        val workflowInstanceRepository: WorkflowInstanceRepository
) : GraphQLQueryResolver {

    fun workflowInstances(count: Int, offset: Int, stateIn: List<WorkflowInstanceState>): List<WorkflowInstance> {
        return workflowInstanceRepository.findByStateIn(stateIn, PageRequest.of(offset, count)).toList()
    }

    fun workflowInstance(key: Long): WorkflowInstance? {
        return workflowInstanceRepository.findByIdOrNull(key)
    }

}
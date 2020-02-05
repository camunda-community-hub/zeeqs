package io.zeebe.zeeqs.data.resolvers

import com.coxautodev.graphql.tools.GraphQLQueryResolver
import io.zeebe.zeeqs.data.entity.WorkflowInstance
import io.zeebe.zeeqs.data.repository.WorkflowInstanceRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class WorkflowInstanceQueryResolver(
        val workflowInstanceRepository: WorkflowInstanceRepository
) : GraphQLQueryResolver {

    fun workflowInstances(count: Int, offset: Int): List<WorkflowInstance> {
        return workflowInstanceRepository.findAll(PageRequest.of(offset, count)).toList()
    }

    fun workflowInstance(key: Long): WorkflowInstance? {
        return workflowInstanceRepository.findByIdOrNull(key)
    }

}
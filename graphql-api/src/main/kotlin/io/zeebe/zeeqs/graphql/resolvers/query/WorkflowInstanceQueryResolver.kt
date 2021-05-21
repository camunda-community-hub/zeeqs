package io.zeebe.zeeqs.data.resolvers

import graphql.kickstart.tools.GraphQLQueryResolver
import io.zeebe.zeeqs.data.entity.ProcessIntance
import io.zeebe.zeeqs.data.entity.ProcessInstanceState
import io.zeebe.zeeqs.data.repository.WorkflowInstanceRepository
import io.zeebe.zeeqs.graphql.resolvers.connection.WorkflowInstanceConnection
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class WorkflowInstanceQueryResolver(
        val workflowInstanceRepository: WorkflowInstanceRepository
) : GraphQLQueryResolver {

    fun workflowInstances(perPage: Int, page: Int, stateIn: List<ProcessInstanceState>): WorkflowInstanceConnection {
        return WorkflowInstanceConnection(
                getItems = { workflowInstanceRepository.findByStateIn(stateIn, PageRequest.of(page, perPage)).toList() },
                getCount = { workflowInstanceRepository.countByStateIn(stateIn) }
        )
    }

    fun workflowInstance(key: Long): ProcessIntance? {
        return workflowInstanceRepository.findByIdOrNull(key)
    }

}
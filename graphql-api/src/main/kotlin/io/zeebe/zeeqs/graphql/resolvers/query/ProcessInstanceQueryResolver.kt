package io.zeebe.zeeqs.data.resolvers

import graphql.kickstart.tools.GraphQLQueryResolver
import io.zeebe.zeeqs.data.entity.ProcessInstance
import io.zeebe.zeeqs.data.entity.ProcessInstanceState
import io.zeebe.zeeqs.data.repository.ProcessInstanceRepository
import io.zeebe.zeeqs.graphql.resolvers.connection.ProcessInstanceConnection
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class ProcessInstanceQueryResolver(
        val processInstanceRepository: ProcessInstanceRepository
) : GraphQLQueryResolver {

    fun processInstances(perPage: Int, page: Int, stateIn: List<ProcessInstanceState>): ProcessInstanceConnection {
        return ProcessInstanceConnection(
                getItems = { processInstanceRepository.findByStateIn(stateIn, PageRequest.of(page, perPage)).toList() },
                getCount = { processInstanceRepository.countByStateIn(stateIn) }
        )
    }

    fun processInstance(key: Long): ProcessInstance? {
        return processInstanceRepository.findByIdOrNull(key)
    }

}
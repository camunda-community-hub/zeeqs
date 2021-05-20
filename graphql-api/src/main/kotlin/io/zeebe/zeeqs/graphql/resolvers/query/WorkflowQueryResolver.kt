package io.zeebe.zeeqs.data.resolvers

import graphql.kickstart.tools.GraphQLQueryResolver
import io.zeebe.zeeqs.data.entity.Process
import io.zeebe.zeeqs.data.repository.ProcessRepository
import io.zeebe.zeeqs.graphql.resolvers.connection.WorkflowConnection
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class WorkflowQueryResolver(
        val processRepository: ProcessRepository
) : GraphQLQueryResolver {

    fun workflows(perPage: Int, page: Int): WorkflowConnection {
        return WorkflowConnection(
                getItems = { processRepository.findAll(PageRequest.of(page, perPage)).toList() },
                getCount = { processRepository.count() }
        )
    }

    fun getWorkflow(key: Long): Process? {
        return processRepository.findByIdOrNull(key)
    }

}
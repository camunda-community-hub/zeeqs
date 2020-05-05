package io.zeebe.zeeqs.data.resolvers

import com.coxautodev.graphql.tools.GraphQLQueryResolver
import io.zeebe.zeeqs.data.entity.Workflow
import io.zeebe.zeeqs.data.repository.WorkflowRepository
import io.zeebe.zeeqs.graphql.resolvers.connection.WorkflowConnection
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class WorkflowQueryResolver(
        val workflowRepository: WorkflowRepository
) : GraphQLQueryResolver {

    fun workflows(limit: Int, page: Int): WorkflowConnection {
        return WorkflowConnection(
                getItems = { workflowRepository.findAll(PageRequest.of(page, limit)).toList() },
                getCount = { workflowRepository.count() }
        )
    }

    fun getWorkflow(key: Long): Workflow? {
        return workflowRepository.findByIdOrNull(key)
    }

}
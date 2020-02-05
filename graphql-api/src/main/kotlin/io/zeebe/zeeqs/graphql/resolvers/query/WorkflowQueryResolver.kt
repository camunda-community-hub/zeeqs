package io.zeebe.zeeqs.data.resolvers

import com.coxautodev.graphql.tools.GraphQLQueryResolver
import io.zeebe.zeeqs.data.entity.Workflow
import io.zeebe.zeeqs.data.repository.WorkflowRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class WorkflowQueryResolver(
        val workflowRepository: WorkflowRepository
) : GraphQLQueryResolver {

    fun workflows(count: Int, offset: Int): List<Workflow> {
        return workflowRepository.findAll(PageRequest.of(offset, count)).toList()
    }

    fun getWorkflow(key: Long): Workflow? {
        return workflowRepository.findByIdOrNull(key)
    }

}
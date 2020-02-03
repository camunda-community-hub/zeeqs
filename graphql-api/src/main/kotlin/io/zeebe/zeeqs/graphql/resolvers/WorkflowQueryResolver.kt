package io.zeebe.zeeqs.data.resolvers

import com.coxautodev.graphql.tools.GraphQLQueryResolver
import io.zeebe.zeeqs.data.entity.Workflow
import io.zeebe.zeeqs.data.repository.WorkflowInstanceRepository
import io.zeebe.zeeqs.data.repository.WorkflowRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class WorkflowQueryResolver(
        val workflowRepository: WorkflowRepository
) : GraphQLQueryResolver {

    fun getWorkflows(count: Int, offset: Int): List<Workflow> {
        val workflows = workflowRepository.findAll(PageRequest.of(offset, count))

        return workflows.toList().toList()
    }

    fun getWorkflow(key: Long): Workflow? {
        return workflowRepository.findByIdOrNull(key)
    }

}
package io.zeebe.zeeqs.data.resolvers

import graphql.kickstart.tools.GraphQLResolver
import io.zeebe.zeeqs.data.entity.Error
import io.zeebe.zeeqs.data.entity.WorkflowInstance
import io.zeebe.zeeqs.data.repository.WorkflowInstanceRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class ErrorResolver(
        val workflowInstanceRepository: WorkflowInstanceRepository
) : GraphQLResolver<Error> {

    fun workflowInstance(error: Error): WorkflowInstance? {
        return error.workflowInstanceKey?.let { workflowInstanceRepository.findByIdOrNull(it) }
    }

}
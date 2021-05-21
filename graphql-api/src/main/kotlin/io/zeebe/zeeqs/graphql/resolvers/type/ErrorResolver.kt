package io.zeebe.zeeqs.data.resolvers

import graphql.kickstart.tools.GraphQLResolver
import io.zeebe.zeeqs.data.entity.Error
import io.zeebe.zeeqs.data.entity.ProcessIntance
import io.zeebe.zeeqs.data.repository.WorkflowInstanceRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class ErrorResolver(
        val workflowInstanceRepository: WorkflowInstanceRepository
) : GraphQLResolver<Error> {

    fun workflowInstance(error: Error): ProcessIntance? {
        return error.processInstanceKey?.let { workflowInstanceRepository.findByIdOrNull(it) }
    }

}
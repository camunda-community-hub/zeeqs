package io.zeebe.zeeqs.data.resolvers

import graphql.kickstart.tools.GraphQLResolver
import io.zeebe.zeeqs.data.entity.Error
import io.zeebe.zeeqs.data.entity.ProcessInstance
import io.zeebe.zeeqs.data.repository.ProcessInstanceRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class ErrorResolver(
        val processInstanceRepository: ProcessInstanceRepository
) : GraphQLResolver<Error> {

    fun processInstance(error: Error): ProcessInstance? {
        return error.processInstanceKey?.let { processInstanceRepository.findByIdOrNull(it) }
    }

}
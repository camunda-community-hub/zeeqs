package io.zeebe.zeeqs.graphql.resolvers.type

import io.zeebe.zeeqs.data.entity.Error
import io.zeebe.zeeqs.data.entity.ProcessInstance
import io.zeebe.zeeqs.data.repository.ProcessInstanceRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class ErrorResolver(
        val processInstanceRepository: ProcessInstanceRepository
) {

    @SchemaMapping(typeName = "Error", field = "processInstance")
    fun processInstance(error: Error): ProcessInstance? {
        return error.processInstanceKey?.let { processInstanceRepository.findByIdOrNull(it) }
    }

}
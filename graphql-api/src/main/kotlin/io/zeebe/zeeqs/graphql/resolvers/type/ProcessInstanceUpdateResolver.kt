package io.zeebe.zeeqs.graphql.resolvers.type

import io.zeebe.zeeqs.data.entity.ProcessInstance
import io.zeebe.zeeqs.data.reactive.ProcessInstanceUpdate
import io.zeebe.zeeqs.data.repository.ProcessInstanceRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class ProcessInstanceUpdateResolver(
        private val processInstanceRepository: ProcessInstanceRepository
) {

    @SchemaMapping(typeName = "ProcessInstanceUpdate", field = "processInstance")
    fun processInstance(update: ProcessInstanceUpdate): ProcessInstance? {
        return processInstanceRepository.findByIdOrNull(update.processInstanceKey)
    }
}
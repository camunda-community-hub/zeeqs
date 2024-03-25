package io.zeebe.zeeqs.graphql.resolvers.query

import io.zeebe.zeeqs.data.entity.ProcessInstance
import io.zeebe.zeeqs.data.entity.ProcessInstanceState
import io.zeebe.zeeqs.data.repository.ProcessInstanceRepository
import io.zeebe.zeeqs.data.service.ProcessInstanceService
import io.zeebe.zeeqs.graphql.resolvers.connection.ProcessInstanceConnection
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class ProcessInstanceQueryResolver(
        val processInstanceRepository: ProcessInstanceRepository,
        val processInstanceService: ProcessInstanceService
) {

    @QueryMapping
    fun processInstances(
            @Argument perPage: Int,
            @Argument page: Int,
            @Argument stateIn: List<ProcessInstanceState>,
            @Argument variableName: String?,
            @Argument variableValue: String?
    ): ProcessInstanceConnection {
        return ProcessInstanceConnection(
                getItems = { processInstanceService.getProcessInstances(perPage, page, stateIn, variableName, variableValue) },
                getCount = { processInstanceService.countProcessInstances(stateIn, variableName, variableValue) }
        )
    }

    @QueryMapping
    fun processInstance(@Argument key: Long): ProcessInstance? {
        return processInstanceRepository.findByIdOrNull(key)
    }

}
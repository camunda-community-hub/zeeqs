package io.zeebe.zeeqs.data.resolvers

import graphql.kickstart.tools.GraphQLQueryResolver
import io.zeebe.zeeqs.data.entity.Process
import io.zeebe.zeeqs.data.repository.ProcessRepository
import io.zeebe.zeeqs.graphql.resolvers.connection.ProcessConnection
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class ProcessQueryResolver(
        val processRepository: ProcessRepository
) : GraphQLQueryResolver {

    fun processes(perPage: Int, page: Int): ProcessConnection {
        return ProcessConnection(
                getItems = { processRepository.findAll(PageRequest.of(page, perPage)).toList() },
                getCount = { processRepository.count() }
        )
    }

    fun getProcess(key: Long): Process? {
        return processRepository.findByIdOrNull(key)
    }

}
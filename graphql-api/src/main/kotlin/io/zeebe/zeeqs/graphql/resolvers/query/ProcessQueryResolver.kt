package io.zeebe.zeeqs.graphql.resolvers.query

import io.zeebe.zeeqs.data.entity.Process
import io.zeebe.zeeqs.data.repository.ProcessRepository
import io.zeebe.zeeqs.graphql.resolvers.connection.ProcessConnection
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class ProcessQueryResolver(
        val processRepository: ProcessRepository
) {

    @QueryMapping
    fun processes(
            @Argument perPage: Int,
            @Argument page: Int
    ): ProcessConnection {
        return ProcessConnection(
                getItems = { processRepository.findAll(PageRequest.of(page, perPage)).toList() },
                getCount = { processRepository.count() }
        )
    }

    @QueryMapping
    fun process(@Argument key: Long): Process? {
        return processRepository.findByIdOrNull(key)
    }

}
package io.zeebe.zeeqs.graphql.resolvers.query

import io.zeebe.zeeqs.data.repository.ErrorRepository
import io.zeebe.zeeqs.graphql.resolvers.connection.ErrorConnection
import org.springframework.data.domain.PageRequest
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class ErrorQueryResolver(
        val errorRepository: ErrorRepository
) {

    @QueryMapping
    fun errors(
            @Argument perPage: Int,
            @Argument page: Int
    ): ErrorConnection {

        return ErrorConnection(
                getItems = { errorRepository.findAll(PageRequest.of(page, perPage)).toList() },
                getCount = { errorRepository.count() }
        )
    }

}
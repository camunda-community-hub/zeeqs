package io.zeebe.zeeqs.data.resolvers

import graphql.kickstart.tools.GraphQLQueryResolver
import io.zeebe.zeeqs.data.repository.ErrorRepository
import io.zeebe.zeeqs.graphql.resolvers.connection.ErrorConnection
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component

@Component
class ErrorQueryResolver(
        val errorRepository: ErrorRepository
) : GraphQLQueryResolver {

    fun errors(
            perPage: Int,
            page: Int
    ): ErrorConnection {

        return ErrorConnection(
                getItems = { errorRepository.findAll(PageRequest.of(page, perPage)).toList() },
                getCount = { errorRepository.count() }
        )
    }

}
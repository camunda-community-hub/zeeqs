package io.zeebe.zeeqs.graphql.resolvers.connection

import graphql.kickstart.tools.GraphQLResolver
import io.zeebe.zeeqs.data.entity.Error
import org.springframework.stereotype.Component

@Component
class ErrorConnectionResolver: GraphQLResolver<ErrorConnection> {

    fun nodes(connection: ErrorConnection): List<Error> {
        return connection.getItems()
    }

    fun totalCount(connection: ErrorConnection): Long {
        return connection.getCount()
    }

}
package io.zeebe.zeeqs.graphql.resolvers.connection

import graphql.kickstart.tools.GraphQLResolver
import io.zeebe.zeeqs.data.entity.Process
import org.springframework.stereotype.Component

@Component
class ProcessConnectionResolver : GraphQLResolver<ProcessConnection> {

    fun nodes(connection: ProcessConnection): List<Process> {
        return connection.getItems()
    }

    fun totalCount(connection: ProcessConnection): Long {
        return connection.getCount()
    }

}
package io.zeebe.zeeqs.graphql.resolvers.connection

import graphql.kickstart.tools.GraphQLResolver
import io.zeebe.zeeqs.data.entity.ProcessInstance
import org.springframework.stereotype.Component

@Component
class ProcessInstanceConnectionResolver : GraphQLResolver<ProcessInstanceConnection> {

    fun nodes(connection: ProcessInstanceConnection): List<ProcessInstance> {
        return connection.getItems()
    }

    fun totalCount(connection: ProcessInstanceConnection): Long {
        return connection.getCount()
    }

}
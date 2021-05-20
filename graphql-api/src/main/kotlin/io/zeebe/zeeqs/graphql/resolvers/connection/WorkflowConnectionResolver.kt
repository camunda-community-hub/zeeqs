package io.zeebe.zeeqs.graphql.resolvers.connection

import graphql.kickstart.tools.GraphQLResolver
import io.zeebe.zeeqs.data.entity.Process
import org.springframework.stereotype.Component

@Component
class WorkflowConnectionResolver : GraphQLResolver<WorkflowConnection> {

    fun nodes(connection: WorkflowConnection): List<Process> {
        return connection.getItems()
    }

    fun totalCount(connection: WorkflowConnection): Long {
        return connection.getCount()
    }

}
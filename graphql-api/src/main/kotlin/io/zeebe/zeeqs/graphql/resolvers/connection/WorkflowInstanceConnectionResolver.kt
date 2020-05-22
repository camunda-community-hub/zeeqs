package io.zeebe.zeeqs.graphql.resolvers.connection

import graphql.kickstart.tools.GraphQLResolver
import io.zeebe.zeeqs.data.entity.WorkflowInstance
import org.springframework.stereotype.Component

@Component
class WorkflowInstanceConnectionResolver : GraphQLResolver<WorkflowInstanceConnection> {

    fun nodes(connection: WorkflowInstanceConnection): List<WorkflowInstance> {
        return connection.getItems()
    }

    fun totalCount(connection: WorkflowInstanceConnection): Long {
        return connection.getCount()
    }

}
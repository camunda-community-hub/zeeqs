package io.zeebe.zeeqs.graphql.resolvers.connection

import graphql.kickstart.tools.GraphQLResolver
import io.zeebe.zeeqs.data.entity.ProcessIntance
import org.springframework.stereotype.Component

@Component
class WorkflowInstanceConnectionResolver : GraphQLResolver<WorkflowInstanceConnection> {

    fun nodes(connection: WorkflowInstanceConnection): List<ProcessIntance> {
        return connection.getItems()
    }

    fun totalCount(connection: WorkflowInstanceConnection): Long {
        return connection.getCount()
    }

}
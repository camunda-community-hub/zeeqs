package io.zeebe.zeeqs.graphql.resolvers.connection

import com.coxautodev.graphql.tools.GraphQLResolver
import io.zeebe.zeeqs.data.entity.Workflow
import org.springframework.stereotype.Component

@Component
class WorkflowConnectionResolver : GraphQLResolver<WorkflowConnection> {

    fun nodes(connection: WorkflowConnection): List<Workflow> {
        return connection.getItems()
    }

    fun totalCount(connection: WorkflowConnection): Long {
        return connection.getCount()
    }

}
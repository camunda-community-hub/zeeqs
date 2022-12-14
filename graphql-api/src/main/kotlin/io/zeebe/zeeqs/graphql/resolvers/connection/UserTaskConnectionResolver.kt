package io.zeebe.zeeqs.graphql.resolvers.connection

import graphql.kickstart.tools.GraphQLResolver
import io.zeebe.zeeqs.data.entity.UserTask
import org.springframework.stereotype.Component

@Component
class UserTaskConnectionResolver: GraphQLResolver<UserTaskConnection> {

    fun nodes(connection: UserTaskConnection): List<UserTask> {
        return connection.getItems()
    }

    fun totalCount(connection: UserTaskConnection): Long {
        return connection.getCount()
    }

}
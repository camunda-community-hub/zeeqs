package io.zeebe.zeeqs.graphql.resolvers.connection

import com.coxautodev.graphql.tools.GraphQLResolver
import io.zeebe.zeeqs.data.entity.Job
import org.springframework.stereotype.Component

@Component
class JobConnectionResolver : GraphQLResolver<JobConnection> {

    fun nodes(connection: JobConnection): List<Job> {
        return connection.getItems()
    }

    fun totalCount(connection: JobConnection): Long {
        return connection.getCount()
    }

}
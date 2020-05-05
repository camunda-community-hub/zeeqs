package io.zeebe.zeeqs.graphql.resolvers.connection

import com.coxautodev.graphql.tools.GraphQLResolver
import io.zeebe.zeeqs.data.entity.Incident
import org.springframework.stereotype.Component

@Component
class IncidentConnectionResolver : GraphQLResolver<IncidentConnection> {

    fun nodes(connection: IncidentConnection): List<Incident> {
        return connection.getItems()
    }

    fun totalCount(connection: IncidentConnection): Long {
        return connection.getCount()
    }

}
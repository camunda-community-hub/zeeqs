package io.zeebe.zeeqs.graphql.resolvers.connection

import io.zeebe.zeeqs.data.entity.Incident
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class IncidentConnectionResolver {

    @SchemaMapping(typeName = "IncidentConnection", field = "nodes")
    fun nodes(connection: IncidentConnection): List<Incident> {
        return connection.getItems()
    }

    @SchemaMapping(typeName = "IncidentConnection", field = "totalCount")
    fun totalCount(connection: IncidentConnection): Long {
        return connection.getCount()
    }

}
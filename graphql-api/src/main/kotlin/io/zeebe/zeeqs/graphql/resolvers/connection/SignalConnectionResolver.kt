package io.zeebe.zeeqs.graphql.resolvers.connection

import io.zeebe.zeeqs.data.entity.Signal
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class SignalConnectionResolver {

    @SchemaMapping(typeName = "SignalConnection", field = "nodes")
    fun nodes(connection: SignalConnection): List<Signal> {
        return connection.getItems()
    }

    @SchemaMapping(typeName = "SignalConnection", field = "totalCount")
    fun totalCount(connection: SignalConnection): Long {
        return connection.getCount()
    }

}
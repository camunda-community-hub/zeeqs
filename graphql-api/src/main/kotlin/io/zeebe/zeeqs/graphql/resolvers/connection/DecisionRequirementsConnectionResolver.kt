package io.zeebe.zeeqs.graphql.resolvers.connection

import io.zeebe.zeeqs.data.entity.DecisionRequirements
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class DecisionRequirementsConnectionResolver {

    @SchemaMapping(typeName = "DecisionRequirementsConnection", field = "nodes")
    fun nodes(connection: DecisionRequirementsConnection): List<DecisionRequirements> {
        return connection.getItems()
    }

    @SchemaMapping(typeName = "DecisionRequirementsConnection", field = "totalCount")
    fun totalCount(connection: DecisionRequirementsConnection): Long {
        return connection.getCount()
    }
}
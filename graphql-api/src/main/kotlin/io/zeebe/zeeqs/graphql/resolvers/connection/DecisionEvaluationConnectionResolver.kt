package io.zeebe.zeeqs.graphql.resolvers.connection

import io.zeebe.zeeqs.data.entity.DecisionEvaluation
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class DecisionEvaluationConnectionResolver {

    @SchemaMapping(typeName = "DecisionEvaluationConnection", field = "nodes")
    fun nodes(connection: DecisionEvaluationConnection): List<DecisionEvaluation> {
        return connection.getItems()
    }

    @SchemaMapping(typeName = "DecisionEvaluationConnection", field = "totalCount")
    fun totalCount(connection: DecisionEvaluationConnection): Long {
        return connection.getCount()
    }
}
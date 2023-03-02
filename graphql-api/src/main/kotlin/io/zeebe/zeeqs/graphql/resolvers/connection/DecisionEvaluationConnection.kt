package io.zeebe.zeeqs.graphql.resolvers.connection

import io.zeebe.zeeqs.data.entity.DecisionEvaluation

class DecisionEvaluationConnection(
    val getItems: () -> List<DecisionEvaluation>,
    val getCount: () -> Long
)
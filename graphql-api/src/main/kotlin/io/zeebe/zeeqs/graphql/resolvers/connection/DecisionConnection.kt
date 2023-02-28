package io.zeebe.zeeqs.graphql.resolvers.connection

import io.zeebe.zeeqs.data.entity.Decision

class DecisionConnection(
    val getItems: () -> List<Decision>,
    val getCount: () -> Long
)
package io.zeebe.zeeqs.graphql.resolvers.connection

import io.zeebe.zeeqs.data.entity.DecisionRequirements

class DecisionRequirementsConnection(
    val getItems: () -> List<DecisionRequirements>,
    val getCount: () -> Long
)
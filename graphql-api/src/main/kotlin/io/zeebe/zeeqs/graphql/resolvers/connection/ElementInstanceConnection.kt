package io.zeebe.zeeqs.graphql.resolvers.connection

import io.zeebe.zeeqs.data.entity.ElementInstance

class ElementInstanceConnection(
    val getItems: () -> List<ElementInstance>,
    val getCount: () -> Long
)
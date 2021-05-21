package io.zeebe.zeeqs.graphql.resolvers.connection

import io.zeebe.zeeqs.data.entity.ProcessInstance

class ProcessInstanceConnection(
    val getItems: () -> List<ProcessInstance>,
    val getCount: () -> Long
)
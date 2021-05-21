package io.zeebe.zeeqs.graphql.resolvers.connection

import io.zeebe.zeeqs.data.entity.Process

class ProcessConnection(
    val getItems: () -> List<Process>,
    val getCount: () -> Long
)
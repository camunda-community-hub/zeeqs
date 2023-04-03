package io.zeebe.zeeqs.graphql.resolvers.connection

import io.zeebe.zeeqs.data.entity.Signal

class SignalConnection(
    val getItems: () -> List<Signal>,
    val getCount: () -> Long
)
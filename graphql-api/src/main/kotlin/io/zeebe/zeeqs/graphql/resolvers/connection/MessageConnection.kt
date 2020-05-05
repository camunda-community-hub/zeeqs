package io.zeebe.zeeqs.graphql.resolvers.connection

import io.zeebe.zeeqs.data.entity.Message

class MessageConnection(
        val getItems: () -> List<Message>,
        val getCount: () -> Long
)
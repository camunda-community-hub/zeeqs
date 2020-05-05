package io.zeebe.zeeqs.graphql.resolvers.type

import io.zeebe.zeeqs.data.entity.Message

class MessageList(
        val getItems: () -> List<Message>,
        val getCount: () -> Long
)
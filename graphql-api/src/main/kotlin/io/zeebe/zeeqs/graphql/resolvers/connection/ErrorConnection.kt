package io.zeebe.zeeqs.graphql.resolvers.connection

import io.zeebe.zeeqs.data.entity.Error

class ErrorConnection(
        val getItems: () -> List<Error>,
        val getCount: () -> Long
)
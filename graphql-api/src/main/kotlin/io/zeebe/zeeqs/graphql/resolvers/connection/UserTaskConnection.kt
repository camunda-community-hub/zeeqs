package io.zeebe.zeeqs.graphql.resolvers.connection

import io.zeebe.zeeqs.data.entity.UserTask

class UserTaskConnection(
        val getItems: () -> List<UserTask>,
        val getCount: () -> Long
)
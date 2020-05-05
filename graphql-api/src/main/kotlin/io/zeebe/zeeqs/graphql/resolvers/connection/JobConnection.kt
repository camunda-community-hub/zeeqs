package io.zeebe.zeeqs.graphql.resolvers.connection

import io.zeebe.zeeqs.data.entity.Job

class JobConnection(
        val getItems: () -> List<Job>,
        val getCount: () -> Long
)
package io.zeebe.zeeqs.graphql.resolvers.type

import io.zeebe.zeeqs.data.entity.Job

class JobList(
        val getItems: () -> List<Job>,
        val getCount: () -> Long
)
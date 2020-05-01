package io.zeebe.zeeqs.graphql.resolvers.type

import io.zeebe.zeeqs.data.entity.Workflow

class WorkflowList(
        val getItems: () -> List<Workflow>,
        val getCount: () -> Long
)
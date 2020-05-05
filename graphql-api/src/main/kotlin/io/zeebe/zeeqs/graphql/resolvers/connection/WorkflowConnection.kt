package io.zeebe.zeeqs.graphql.resolvers.connection

import io.zeebe.zeeqs.data.entity.Workflow

class WorkflowConnection(
        val getItems: () -> List<Workflow>,
        val getCount: () -> Long
)
package io.zeebe.zeeqs.graphql.resolvers.type

import io.zeebe.zeeqs.data.entity.WorkflowInstance

class WorkflowInstanceList(
        val getItems: () -> List<WorkflowInstance>,
        val getCount: () -> Long
)
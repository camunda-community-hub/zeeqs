package io.zeebe.zeeqs.graphql.resolvers.connection

import io.zeebe.zeeqs.data.entity.ProcessIntance

class WorkflowInstanceConnection(
    val getItems: () -> List<ProcessIntance>,
    val getCount: () -> Long
)
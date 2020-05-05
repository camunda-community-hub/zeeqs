package io.zeebe.zeeqs.graphql.resolvers.connection

import io.zeebe.zeeqs.data.entity.Incident

class IncidentConnection(
        val getItems: () -> List<Incident>,
        val getCount: () -> Long
)
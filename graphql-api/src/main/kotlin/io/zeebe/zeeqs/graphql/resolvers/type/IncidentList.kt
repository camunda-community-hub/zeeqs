package io.zeebe.zeeqs.graphql.resolvers.type

import io.zeebe.zeeqs.data.entity.Incident

class IncidentList(
        val getItems: () -> List<Incident>,
        val getCount: () -> Long
)
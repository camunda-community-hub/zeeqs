package io.zeebe.zeeqs.graphql.resolvers.type

import com.coxautodev.graphql.tools.GraphQLResolver
import io.zeebe.zeeqs.data.entity.Incident
import org.springframework.stereotype.Component

@Component
class IncidentListResolver : GraphQLResolver<IncidentList> {

    fun items(list: IncidentList): List<Incident> {
        return list.getItems()
    }

    fun totalCount(list: IncidentList): Long {
        return list.getCount()
    }

}
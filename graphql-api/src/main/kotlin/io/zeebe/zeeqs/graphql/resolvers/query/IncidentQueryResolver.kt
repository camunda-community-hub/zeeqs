package io.zeebe.zeeqs.data.resolvers

import graphql.kickstart.tools.GraphQLQueryResolver
import io.zeebe.zeeqs.data.entity.Incident
import io.zeebe.zeeqs.data.entity.IncidentState
import io.zeebe.zeeqs.data.repository.IncidentRepository
import io.zeebe.zeeqs.graphql.resolvers.connection.IncidentConnection
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class IncidentQueryResolver(
        val incidentRepository: IncidentRepository
) : GraphQLQueryResolver {

    fun incidents(
            perPage: Int,
            page: Int,
            stateIn: List<IncidentState>
    ): IncidentConnection {

        return IncidentConnection(
                getItems = {
                    incidentRepository.findByStateIn(
                            stateIn = stateIn,
                            pageable = PageRequest.of(page, perPage)
                    )
                },
                getCount = { incidentRepository.countByStateIn(stateIn = stateIn) }
        )
    }

    fun incident(key: Long): Incident? {
        return incidentRepository.findByIdOrNull(key)
    }

}
package io.zeebe.zeeqs.data.resolvers

import com.coxautodev.graphql.tools.GraphQLQueryResolver
import io.zeebe.zeeqs.data.entity.Incident
import io.zeebe.zeeqs.data.entity.IncidentState
import io.zeebe.zeeqs.data.repository.IncidentRepository
import io.zeebe.zeeqs.graphql.resolvers.type.IncidentList
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class IncidentQueryResolver(
        val incidentRepository: IncidentRepository
) : GraphQLQueryResolver {

    fun incidents(
            limit: Int,
            page: Int,
            stateIn: List<IncidentState>
    ): IncidentList {

        return IncidentList(
                getItems = {
                    incidentRepository.findByStateIn(
                            stateIn = stateIn,
                            pageable = PageRequest.of(page, limit)
                    )
                },
                getCount = { incidentRepository.countByStateIn(stateIn = stateIn) }
        )
    }

    fun incident(key: Long): Incident? {
        return incidentRepository.findByIdOrNull(key)
    }

}
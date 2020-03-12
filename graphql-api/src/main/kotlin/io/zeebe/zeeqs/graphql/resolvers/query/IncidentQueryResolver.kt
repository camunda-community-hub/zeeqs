package io.zeebe.zeeqs.data.resolvers

import com.coxautodev.graphql.tools.GraphQLQueryResolver
import io.zeebe.zeeqs.data.entity.Incident
import io.zeebe.zeeqs.data.entity.IncidentState
import io.zeebe.zeeqs.data.repository.IncidentRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class IncidentQueryResolver(
        val incidentRepository: IncidentRepository
) : GraphQLQueryResolver {

    fun incidents(
            count: Int,
            offset: Int,
            stateIn: List<IncidentState>
    ): List<Incident> {

        return incidentRepository.findByStateIn(
                stateIn = stateIn,
                pageable = PageRequest.of(offset, count)
        )
    }

    fun incident(key: Long): Incident? {
        return incidentRepository.findByIdOrNull(key)
    }

}
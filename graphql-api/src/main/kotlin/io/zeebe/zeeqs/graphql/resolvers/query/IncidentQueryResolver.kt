package io.zeebe.zeeqs.data.resolvers

import com.coxautodev.graphql.tools.GraphQLQueryResolver
import io.zeebe.zeeqs.data.entity.Incident
import io.zeebe.zeeqs.data.repository.IncidentRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class IncidentQueryResolver(
        val incidentRepository: IncidentRepository
) : GraphQLQueryResolver {

    fun incidents(count: Int, offset: Int): List<Incident> {
        return incidentRepository.findAll(PageRequest.of(offset, count)).toList()
    }

    fun incident(key: Long): Incident? {
        return incidentRepository.findByIdOrNull(key)
    }

}
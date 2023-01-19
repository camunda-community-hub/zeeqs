package io.zeebe.zeeqs.graphql.resolvers.query

import io.zeebe.zeeqs.data.entity.Incident
import io.zeebe.zeeqs.data.entity.IncidentState
import io.zeebe.zeeqs.data.repository.IncidentRepository
import io.zeebe.zeeqs.graphql.resolvers.connection.IncidentConnection
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class IncidentQueryResolver(
        val incidentRepository: IncidentRepository
) {

    @QueryMapping
    fun incidents(
            @Argument perPage: Int,
            @Argument page: Int,
            @Argument stateIn: List<IncidentState>
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

    @QueryMapping
    fun incident(@Argument key: Long): Incident? {
        return incidentRepository.findByIdOrNull(key)
    }

}
package io.zeebe.zeeqs.graphql.resolvers.type

import io.zeebe.zeeqs.data.entity.ElementInstance
import io.zeebe.zeeqs.data.entity.Incident
import io.zeebe.zeeqs.data.entity.Job
import io.zeebe.zeeqs.data.entity.ProcessInstance
import io.zeebe.zeeqs.data.repository.ElementInstanceRepository
import io.zeebe.zeeqs.data.repository.JobRepository
import io.zeebe.zeeqs.data.repository.ProcessInstanceRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class IncidentResolver(
        val processInstanceRepository: ProcessInstanceRepository,
        val jobRepository: JobRepository,
        val elementInstanceRepository: ElementInstanceRepository
) {

    @SchemaMapping(typeName = "Incident", field = "creationTime")
    fun creationTime(
            incident: Incident,
            @Argument zoneId: String
    ): String? {
        return incident.creationTime?.let { ResolverExtension.timestampToString(it, zoneId) }
    }

    @SchemaMapping(typeName = "Incident", field = "resolveTime")
    fun resolveTime(
            incident: Incident,
            @Argument zoneId: String
    ): String? {
        return incident.resolveTime?.let { ResolverExtension.timestampToString(it, zoneId) }
    }

    @SchemaMapping(typeName = "Incident", field = "processInstance")
    fun processInstance(incident: Incident): ProcessInstance? {
        return processInstanceRepository.findByIdOrNull(incident.processInstanceKey)
    }

    @SchemaMapping(typeName = "Incident", field = "job")
    fun job(incident: Incident): Job? {
        return incident.jobKey?.let { jobRepository.findByIdOrNull(it) }
    }

    @SchemaMapping(typeName = "Incident", field = "elementInstance")
    fun elementInstance(incident: Incident): ElementInstance? {
        return elementInstanceRepository.findByIdOrNull(incident.elementInstanceKey)
    }

}
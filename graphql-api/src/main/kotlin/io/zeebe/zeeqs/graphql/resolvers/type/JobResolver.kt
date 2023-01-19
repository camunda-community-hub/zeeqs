package io.zeebe.zeeqs.graphql.resolvers.type

import io.zeebe.zeeqs.data.entity.ElementInstance
import io.zeebe.zeeqs.data.entity.Incident
import io.zeebe.zeeqs.data.entity.Job
import io.zeebe.zeeqs.data.entity.ProcessInstance
import io.zeebe.zeeqs.data.repository.ElementInstanceRepository
import io.zeebe.zeeqs.data.repository.IncidentRepository
import io.zeebe.zeeqs.data.repository.ProcessInstanceRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class JobResolver(
        val processInstanceRepository: ProcessInstanceRepository,
        val incidentRepository: IncidentRepository,
        val elementInstanceRepository: ElementInstanceRepository
) {

    @SchemaMapping(typeName = "Job", field = "timestamp")
    fun timestamp(
            job: Job,
            @Argument zoneId: String
    ): String? {
        return job.timestamp.let { ResolverExtension.timestampToString(it, zoneId) }
    }

    @SchemaMapping(typeName = "Job", field = "startTime")
    fun startTime(
            job: Job,
            @Argument zoneId: String
    ): String? {
        return job.startTime?.let { ResolverExtension.timestampToString(it, zoneId) }
    }

    @SchemaMapping(typeName = "Job", field = "endTime")
    fun endTime(
            job: Job,
            @Argument zoneId: String
    ): String? {
        return job.endTime?.let { ResolverExtension.timestampToString(it, zoneId) }
    }

    @SchemaMapping(typeName = "Job", field = "processInstance")
    fun processInstance(job: Job): ProcessInstance? {
        return processInstanceRepository.findByIdOrNull(job.processInstanceKey)
    }

    @SchemaMapping(typeName = "Job", field = "incidents")
    fun incidents(job: Job): List<Incident> {
        return incidentRepository.findByJobKey(job.key)
    }

    @SchemaMapping(typeName = "Job", field = "elementInstance")
    fun elementInstance(job: Job): ElementInstance? {
        return elementInstanceRepository.findByIdOrNull(job.elementInstanceKey)
    }

}
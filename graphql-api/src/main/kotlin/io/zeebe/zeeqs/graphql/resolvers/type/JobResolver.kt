package io.zeebe.zeeqs.data.resolvers

import graphql.kickstart.tools.GraphQLResolver
import io.zeebe.zeeqs.data.entity.ElementInstance
import io.zeebe.zeeqs.data.entity.Incident
import io.zeebe.zeeqs.data.entity.Job
import io.zeebe.zeeqs.data.entity.ProcessIntance
import io.zeebe.zeeqs.data.repository.ElementInstanceRepository
import io.zeebe.zeeqs.data.repository.IncidentRepository
import io.zeebe.zeeqs.data.repository.WorkflowInstanceRepository
import io.zeebe.zeeqs.graphql.resolvers.type.ResolverExtension
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class JobResolver(
        val workflowInstanceRepository: WorkflowInstanceRepository,
        val incidentRepository: IncidentRepository,
        val elementInstanceRepository: ElementInstanceRepository
) : GraphQLResolver<Job> {

    fun timestamp(job: Job, zoneId: String): String? {
        return job.timestamp.let { ResolverExtension.timestampToString(it, zoneId) }
    }

    fun startTime(job: Job, zoneId: String): String? {
        return job.startTime?.let { ResolverExtension.timestampToString(it, zoneId) }
    }

    fun endTime(job: Job, zoneId: String): String? {
        return job.endTime?.let { ResolverExtension.timestampToString(it, zoneId) }
    }

    fun workflowInstance(job: Job): ProcessIntance? {
        return workflowInstanceRepository.findByIdOrNull(job.processInstanceKey)
    }

    fun incidents(job: Job): List<Incident> {
        return incidentRepository.findByJobKey(job.key)
    }

    fun elementInstance(job: Job): ElementInstance? {
        return elementInstanceRepository.findByIdOrNull(job.elementInstanceKey)
    }

}
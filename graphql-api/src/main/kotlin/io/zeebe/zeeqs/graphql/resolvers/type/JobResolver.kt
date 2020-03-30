package io.zeebe.zeeqs.data.resolvers

import com.coxautodev.graphql.tools.GraphQLResolver
import io.zeebe.zeeqs.data.entity.*
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

    fun workflowInstance(job: Job): WorkflowInstance? {
        return workflowInstanceRepository.findByIdOrNull(job.workflowInstanceKey)
    }

    fun incidents(job: Job): List<Incident> {
        return incidentRepository.findByJobKey(job.key)
    }

    fun elementInstance(job: Job): ElementInstance? {
        return elementInstanceRepository.findByIdOrNull(job.elementInstanceKey)
    }

}
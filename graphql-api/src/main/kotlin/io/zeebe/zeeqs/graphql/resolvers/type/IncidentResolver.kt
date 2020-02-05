package io.zeebe.zeeqs.data.resolvers

import com.coxautodev.graphql.tools.GraphQLResolver
import io.zeebe.zeeqs.data.entity.ElementInstance
import io.zeebe.zeeqs.data.entity.Incident
import io.zeebe.zeeqs.data.entity.Job
import io.zeebe.zeeqs.data.entity.WorkflowInstance
import io.zeebe.zeeqs.data.repository.ElementInstanceRepository
import io.zeebe.zeeqs.data.repository.JobRepository
import io.zeebe.zeeqs.data.repository.WorkflowInstanceRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class IncidentResolver(
        val workflowInstanceRepository: WorkflowInstanceRepository,
        val jobRepository: JobRepository,
        val elementInstanceRepository: ElementInstanceRepository
) : GraphQLResolver<Incident> {

    fun workflowInstance(incident: Incident): WorkflowInstance? {
        return workflowInstanceRepository.findByIdOrNull(incident.workflowInstanceKey)
    }

    fun job(incident: Incident): Job? {
        return incident.jobKey?.let { jobRepository.findByIdOrNull(it) }
    }

    fun elementInstance(incident: Incident): ElementInstance? {
        return elementInstanceRepository.findByIdOrNull(incident.elementInstanceKey)
    }

}
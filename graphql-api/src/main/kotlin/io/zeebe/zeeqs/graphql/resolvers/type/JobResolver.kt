package io.zeebe.zeeqs.data.resolvers

import com.coxautodev.graphql.tools.GraphQLResolver
import io.zeebe.zeeqs.data.entity.Job
import io.zeebe.zeeqs.data.entity.WorkflowInstance
import io.zeebe.zeeqs.data.repository.WorkflowInstanceRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class JobResolver(
        val workflowInstanceRepository: WorkflowInstanceRepository
) : GraphQLResolver<Job> {

    fun workflowInstance(job: Job): WorkflowInstance? {
        return workflowInstanceRepository.findByIdOrNull(job.workflowInstanceKey)
    }

}
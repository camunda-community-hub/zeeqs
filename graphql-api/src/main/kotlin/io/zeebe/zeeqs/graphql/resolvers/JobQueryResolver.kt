package io.zeebe.zeeqs.data.resolvers

import com.coxautodev.graphql.tools.GraphQLQueryResolver
import io.zeebe.zeeqs.data.entity.Job
import io.zeebe.zeeqs.data.entity.WorkflowInstance
import io.zeebe.zeeqs.data.repository.JobRepository
import io.zeebe.zeeqs.data.repository.WorkflowInstanceRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class JobQueryResolver(
        val jobRepository: JobRepository,
        val workflowInstanceRepository: WorkflowInstanceRepository
) : GraphQLQueryResolver {

    fun getJobs(count: Int, offset: Int): List<Job> {
        val jobs = jobRepository.findAll(PageRequest.of(offset, count))

        for (job in jobs) {
            transformJob(job)
        }

        return jobs.toList().toList()
    }

    private fun transformJob(job: Job): Job {
        job.workflowInstance = getWorkflowInstance(job.workflowInstanceKey)
        return job
    }

    private fun getWorkflowInstance(workflowInstanceKey: Long): WorkflowInstance? {
        return workflowInstanceRepository.findByIdOrNull(workflowInstanceKey)
    }

    fun job(key: Long): Job? {
        return jobRepository.findByIdOrNull(key)?.let { transformJob(it) }
    }

}
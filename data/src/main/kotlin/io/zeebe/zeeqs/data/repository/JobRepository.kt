package io.zeebe.zeeqs.data.repository

import io.zeebe.zeeqs.data.entity.Job
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface JobRepository : PagingAndSortingRepository<Job, Long> {

    fun findByWorkflowInstanceKey(workflowInstanceKey: Long): List<Job>

}
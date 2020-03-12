package io.zeebe.zeeqs.data.repository

import io.zeebe.zeeqs.data.entity.Job
import io.zeebe.zeeqs.data.entity.JobState
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface JobRepository : PagingAndSortingRepository<Job, Long> {

    fun findByWorkflowInstanceKey(workflowInstanceKey: Long): List<Job>

    fun findByWorkflowInstanceKeyAndStateIn(workflowInstanceKey: Long, stateIn: List<JobState>): List<Job>

    fun findByWorkflowInstanceKeyAndStateInAndJobTypeIn(workflowInstanceKey: Long, stateIn: List<JobState>, jobTypeIn: List<String>): List<Job>

    fun findByStateIn(stateIn: List<JobState>, pageable: Pageable): List<Job>

    fun findByJobTypeIn(jobTypeIn: List<String>, pageable: Pageable): List<Job>

    fun findByStateInAndJobTypeIn(stateIn: List<JobState>, jobTypeIn: List<String>, pageable: Pageable): List<Job>

}
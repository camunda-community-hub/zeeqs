package io.zeebe.zeeqs.data.repository

import io.zeebe.zeeqs.data.entity.Job
import io.zeebe.zeeqs.data.entity.JobState
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface JobRepository : PagingAndSortingRepository<Job, Long> {

    fun findByProcessInstanceKey(processInstanceKey: Long): List<Job>

    fun findByProcessInstanceKeyAndStateIn(processInstanceKey: Long, stateIn: List<JobState>): List<Job>

    fun findByProcessInstanceKeyAndStateInAndJobTypeIn(processInstanceKey: Long, stateIn: List<JobState>, jobTypeIn: List<String>): List<Job>

    fun findByStateIn(stateIn: List<JobState>, pageable: Pageable): List<Job>

    fun countByStateIn(stateIn: List<JobState>): Long

    fun findByJobTypeIn(jobTypeIn: List<String>, pageable: Pageable): List<Job>

    fun findByStateInAndJobTypeIn(stateIn: List<JobState>, jobTypeIn: List<String>, pageable: Pageable): List<Job>

    fun countByStateInAndJobTypeIn(stateIn: List<JobState>, jobTypeIn: List<String>): Long

}
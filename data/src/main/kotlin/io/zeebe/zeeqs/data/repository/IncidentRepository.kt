package io.zeebe.zeeqs.data.repository

import io.zeebe.zeeqs.data.entity.Incident
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface IncidentRepository : PagingAndSortingRepository<Incident, Long> {

    fun findByWorkflowInstanceKey(workflowInstanceKey: Long): List<Incident>

    fun findByJobKey(jobKey: Long): List<Incident>
}
package io.zeebe.zeeqs.data.repository

import io.zeebe.zeeqs.data.entity.Incident
import io.zeebe.zeeqs.data.entity.IncidentState
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface IncidentRepository : PagingAndSortingRepository<Incident, Long> {

    fun findByWorkflowInstanceKey(workflowInstanceKey: Long): List<Incident>

    fun findByJobKey(jobKey: Long): List<Incident>

    fun findByStateIn(stateIn: List<IncidentState>, pageable: Pageable): List<Incident>

    fun findByWorkflowInstanceKeyAndStateIn(workflowInstanceKey: Long, stateIn: List<IncidentState>): List<Incident>
}
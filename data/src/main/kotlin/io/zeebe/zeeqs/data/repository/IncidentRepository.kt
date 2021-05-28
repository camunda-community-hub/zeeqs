package io.zeebe.zeeqs.data.repository

import io.zeebe.zeeqs.data.entity.Incident
import io.zeebe.zeeqs.data.entity.IncidentState
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface IncidentRepository : PagingAndSortingRepository<Incident, Long> {

    @Transactional(readOnly = true)
    fun findByProcessInstanceKey(processInstanceKey: Long): List<Incident>

    @Transactional(readOnly = true)
    fun findByJobKey(jobKey: Long): List<Incident>

    @Transactional(readOnly = true)
    fun findByStateIn(stateIn: List<IncidentState>, pageable: Pageable): List<Incident>

    fun countByStateIn(stateIn: List<IncidentState>): Long

    @Transactional(readOnly = true)
    fun findByProcessInstanceKeyAndStateIn(processInstanceKey: Long, stateIn: List<IncidentState>): List<Incident>
}
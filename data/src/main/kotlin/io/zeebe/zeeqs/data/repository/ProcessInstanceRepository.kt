package io.zeebe.zeeqs.data.repository

import io.zeebe.zeeqs.data.entity.ProcessInstance
import io.zeebe.zeeqs.data.entity.ProcessInstanceState
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface ProcessInstanceRepository : PagingAndSortingRepository<ProcessInstance, Long>,
    CrudRepository<ProcessInstance, Long> {

    fun findByParentProcessInstanceKey(parentProcessInstanceKey: Long): List<ProcessInstance>

    fun findByProcessDefinitionKeyAndStateIn(
        processDefinitionKey: Long,
        stateIn: List<ProcessInstanceState>,
        pageable: Pageable
    ): List<ProcessInstance>

    fun countByProcessDefinitionKeyAndStateIn(processDefinitionKey: Long, stateIn: List<ProcessInstanceState>): Long

    fun findByStateIn(stateIn: List<ProcessInstanceState>, pageable: Pageable): List<ProcessInstance>

    fun countByStateIn(stateIn: List<ProcessInstanceState>): Long

}
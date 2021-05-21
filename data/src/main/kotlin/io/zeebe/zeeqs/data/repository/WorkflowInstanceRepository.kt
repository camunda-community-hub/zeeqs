package io.zeebe.zeeqs.data.repository

import io.zeebe.zeeqs.data.entity.ProcessIntance
import io.zeebe.zeeqs.data.entity.ProcessInstanceState
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface WorkflowInstanceRepository : PagingAndSortingRepository<ProcessIntance, Long> {

    fun findByParentWorkflowInstanceKey(parentWorkflowInstanceKey: Long): List<ProcessIntance>

    fun findByWorkflowKeyAndStateIn(workflowKey: Long, stateIn: List<ProcessInstanceState>, pageable: Pageable): List<ProcessIntance>

    fun countByWorkflowKeyAndStateIn(workflowKey: Long, stateIn: List<ProcessInstanceState>): Long

    fun findByStateIn(stateIn: List<ProcessInstanceState>, pageable: Pageable): List<ProcessIntance>

    fun countByStateIn(stateIn: List<ProcessInstanceState>): Long

}
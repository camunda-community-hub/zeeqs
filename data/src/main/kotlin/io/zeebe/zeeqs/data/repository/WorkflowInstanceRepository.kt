package io.zeebe.zeeqs.data.repository

import io.zeebe.zeeqs.data.entity.WorkflowInstance
import io.zeebe.zeeqs.data.entity.WorkflowInstanceState
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface WorkflowInstanceRepository : PagingAndSortingRepository<WorkflowInstance, Long> {

    fun findByParentWorkflowInstanceKey(parentWorkflowInstanceKey: Long): List<WorkflowInstance>

    fun findByWorkflowKey(workflowKey: Long): List<WorkflowInstance>

    fun findByWorkflowKeyAndStateIn(workflowKey: Long, stateIn: List<WorkflowInstanceState>): List<WorkflowInstance>

    fun findByStateIn(stateIn: List<WorkflowInstanceState>, pageable: Pageable): List<WorkflowInstance>

}
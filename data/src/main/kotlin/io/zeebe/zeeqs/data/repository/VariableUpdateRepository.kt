package io.zeebe.zeeqs.data.repository

import io.zeebe.zeeqs.data.entity.VariableUpdate
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface VariableUpdateRepository : PagingAndSortingRepository<VariableUpdate, Long> {

    fun findByWorkflowInstanceKey(workflowInstanceKey: Long): List<VariableUpdate>

    fun findByVariableKey(variableKey: Long): List<VariableUpdate>
}
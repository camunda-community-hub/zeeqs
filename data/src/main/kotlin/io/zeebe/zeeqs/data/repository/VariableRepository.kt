package io.zeebe.zeeqs.data.repository

import io.zeebe.zeeqs.data.entity.Variable
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface VariableRepository : PagingAndSortingRepository<Variable, Long> {

    fun findByWorkflowInstanceKey(workflowInstanceKey: Long): List<Variable>
}
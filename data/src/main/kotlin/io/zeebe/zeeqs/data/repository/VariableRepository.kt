package io.zeebe.zeeqs.data.repository

import io.zeebe.zeeqs.data.entity.Variable
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface VariableRepository : PagingAndSortingRepository<Variable, Long>,
    CrudRepository<Variable, Long> {

    @Transactional(readOnly = true)
    fun findByProcessInstanceKey(processInstanceKey: Long): List<Variable>

    @Transactional(readOnly = true)
    fun findByScopeKey(scopeKey: Long): List<Variable>
}
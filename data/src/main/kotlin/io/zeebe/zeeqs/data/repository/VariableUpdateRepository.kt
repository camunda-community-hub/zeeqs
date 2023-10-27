package io.zeebe.zeeqs.data.repository

import io.zeebe.zeeqs.data.entity.VariableUpdate
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface VariableUpdateRepository : PagingAndSortingRepository<VariableUpdate, String>,
    CrudRepository<VariableUpdate, String> {

    @Transactional(readOnly = true)
    fun findByProcessInstanceKey(processInstanceKey: Long): List<VariableUpdate>

    @Transactional(readOnly = true)
    fun findByVariableKey(variableKey: Long): List<VariableUpdate>
}
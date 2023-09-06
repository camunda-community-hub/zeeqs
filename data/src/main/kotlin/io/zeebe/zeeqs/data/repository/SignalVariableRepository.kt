package io.zeebe.zeeqs.data.repository

import io.zeebe.zeeqs.data.entity.SignalVariable
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface SignalVariableRepository : PagingAndSortingRepository<SignalVariable, String>,
    CrudRepository<SignalVariable, String> {

    fun findBySignalKey(signalKey: Long): List<SignalVariable>

}
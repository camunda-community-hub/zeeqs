package io.zeebe.zeeqs.data.repository

import io.zeebe.zeeqs.data.entity.Timer
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface TimerRepository : PagingAndSortingRepository<Timer, Long> {

    fun findByProcessInstanceKey(processInstanceKey: Long): List<Timer>

    fun findByProcessDefinitionKeyAndElementInstanceKeyIsNull(processDefinitionKey: Long): List<Timer>

    fun findByElementInstanceKey(elementInstanceKey: Long): List<Timer>

}
package io.zeebe.zeeqs.data.repository

import io.zeebe.zeeqs.data.entity.UserTask
import io.zeebe.zeeqs.data.entity.UserTaskState
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface UserTaskRepository : PagingAndSortingRepository<UserTask, Long> {

    fun findByProcessInstanceKey(processInstanceKey: Long): List<UserTask>

    fun findByProcessInstanceKeyAndStateIn(processInstanceKey: Long, stateIn: List<UserTaskState>, pageable: Pageable): List<UserTask>

    fun findByStateIn(stateIn: List<UserTaskState>, pageable: Pageable): List<UserTask>

    fun countByStateIn(stateIn: List<UserTaskState>): Long

    fun countByProcessInstanceKeyAndStateIn(processInstanceKey: Long, stateIn: List<UserTaskState>): Long
}
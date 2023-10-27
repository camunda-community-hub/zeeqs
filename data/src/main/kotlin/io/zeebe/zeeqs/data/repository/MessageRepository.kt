package io.zeebe.zeeqs.data.repository

import io.zeebe.zeeqs.data.entity.Message
import io.zeebe.zeeqs.data.entity.MessageState
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface MessageRepository : PagingAndSortingRepository<Message, Long>,
    CrudRepository<Message, Long> {

    fun findByStateIn(stateIn: List<MessageState>, pageable: Pageable): List<Message>

    fun countByStateIn(stateIn: List<MessageState>): Long

}
package io.zeebe.zeeqs.data.repository

import io.zeebe.zeeqs.data.entity.MessageVariable
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface MessageVariableRepository : PagingAndSortingRepository<MessageVariable, String> {

    fun findByMessageKey(messageKey: Long): List<MessageVariable>

}
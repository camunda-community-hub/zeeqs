package io.zeebe.zeeqs.data.repository

import io.zeebe.zeeqs.data.entity.MessageSubscription
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface MessageSubscriptionRepository : PagingAndSortingRepository<MessageSubscription, Long>,
    CrudRepository<MessageSubscription, Long> {

    fun findByProcessInstanceKey(processInstanceKey: Long): List<MessageSubscription>

    fun findByElementInstanceKey(elementInstanceKey: Long): List<MessageSubscription>

    fun findByProcessDefinitionKeyAndElementInstanceKeyIsNull(processDefinitionKey: Long): List<MessageSubscription>

    fun findByElementInstanceKeyAndMessageName(elementInstanceKey: Long, messageName: String): MessageSubscription?

    fun findByProcessDefinitionKeyAndMessageName(processDefinitionKey: Long, messageName: String): MessageSubscription?

}
package io.zeebe.zeeqs.data.repository

import io.zeebe.zeeqs.data.entity.MessageSubscription
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface MessageSubscriptionRepository : PagingAndSortingRepository<MessageSubscription, Long> {

    fun findByWorkflowInstanceKey(workflowInstanceKey: Long): List<MessageSubscription>

    fun findByElementInstanceKey(elementInstanceKey: Long): List<MessageSubscription>

    fun findByWorkflowKey(workflowKey: Long): List<MessageSubscription>

    fun findByElementInstanceKeyAndMessageName(elementInstanceKey: Long, messageName: String): MessageSubscription?

    fun findByWorkflowKeyAndMessageName(workflowKey: Long, messageName: String): MessageSubscription?

}
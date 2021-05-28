package io.zeebe.zeeqs.data.repository

import io.zeebe.zeeqs.data.entity.MessageCorrelation
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface MessageCorrelationRepository : PagingAndSortingRepository<MessageCorrelation, Long> {

    fun findByMessageNameAndElementInstanceKey(messageName: String, elementInstanceKey: Long): List<MessageCorrelation>

    fun findByMessageNameAndProcessDefinitionKey(messageName: String, processDefinitionKey: Long): List<MessageCorrelation>

    fun findByMessageKey(messageKey: Long): List<MessageCorrelation>
}
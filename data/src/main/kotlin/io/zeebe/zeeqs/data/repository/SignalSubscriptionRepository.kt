package io.zeebe.zeeqs.data.repository

import io.zeebe.zeeqs.data.entity.SignalSubscription
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface SignalSubscriptionRepository : PagingAndSortingRepository<SignalSubscription, Long> {

    fun findByProcessDefinitionKey(processDefinitionKey: Long): List<SignalSubscription>

    fun findByProcessDefinitionKeyAndSignalName(
        processDefinitionKey: Long,
        signalName: String
    ): SignalSubscription?

}
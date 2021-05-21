package io.zeebe.zeeqs.data.entity

import javax.persistence.Entity
import javax.persistence.Enumerated
import javax.persistence.EnumType
import javax.persistence.Id

@Entity
class MessageSubscription(
    @Id val key: Long,
    val position: Long,
    val messageName: String,
    val messageCorrelationKey: String?,
    val processInstanceKey: Long?,
    val elementInstanceKey: Long?,
    val processDefinitionKey: Long?,
    val elementId: String?
) {

    @Enumerated(EnumType.STRING)
    var state: MessageSubscriptionState = MessageSubscriptionState.CREATED
    var timestamp: Long = -1
}
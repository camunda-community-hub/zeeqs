package io.zeebe.zeeqs.data.entity

import javax.persistence.*

@Entity
class MessageSubscription(
        @Id @Column(name = "key_") val key: Long,
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
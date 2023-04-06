package io.zeebe.zeeqs.data.entity

import javax.persistence.*

@Entity
class SignalSubscription(
    @Id @Column(name = "key_") val key: Long,
    val position: Long,
    val signalName: String,
    val processDefinitionKey: Long,
    val elementId: String
) {

    @Enumerated(EnumType.STRING)
    var state: SignalSubscriptionState = SignalSubscriptionState.CREATED
    var timestamp: Long = -1
}
package io.zeebe.zeeqs.data.entity

import javax.persistence.*

@Entity
class Message(
        @Id @Column(name = "key_") val key: Long,
        val position: Long,
        val name: String,
        val correlationKey: String?,
        val messageId: String?,
        val timeToLive: Long
) {

    @Enumerated(EnumType.STRING)
    var state: MessageState = MessageState.PUBLISHED
    var timestamp: Long = -1
}
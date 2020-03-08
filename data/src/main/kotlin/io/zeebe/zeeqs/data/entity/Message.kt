package io.zeebe.zeeqs.data.entity

import javax.persistence.Entity
import javax.persistence.Id

@Entity
class Message(
        @Id val key: Long,
        val name: String,
        val correlationKey: String?,
        val messageId: String?,
        val timeToLive: Long
) {

    var state: MessageState = MessageState.PUBLISHED
    var timestamp: Long = -1
}
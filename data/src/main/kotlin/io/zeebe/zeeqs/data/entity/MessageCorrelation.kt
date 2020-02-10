package io.zeebe.zeeqs.data.entity

import javax.persistence.Entity
import javax.persistence.Id

@Entity
class MessageCorrelation(
        @Id val position: Long,
        val messageKey: Long,
        val messageName: String, // would favor subscriptionKey over message name + element instance key
        val elementInstanceKey: Long,
        val timestamp: Long
)
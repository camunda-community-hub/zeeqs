package io.zeebe.zeeqs.data.entity

import javax.persistence.Entity
import javax.persistence.Id

@Entity
class MessageCorrelation(
        @Id val position: Long,
        val messageKey: Long,
        val messageName: String,
        val timestamp: Long,
        val processInstanceKey: Long,
        val elementInstanceKey: Long?,
        val elementId: String?,
        val processDefinitionKey: Long?
)
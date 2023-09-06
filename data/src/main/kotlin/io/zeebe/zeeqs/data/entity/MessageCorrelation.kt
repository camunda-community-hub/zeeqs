package io.zeebe.zeeqs.data.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id


@Entity
class MessageCorrelation(
    @Id val partitionIdWithPosition: String,
    val messageKey: Long,
    val messageName: String,
    val timestamp: Long,
    val processInstanceKey: Long,
    val elementInstanceKey: Long?,
    val elementId: String?,
    val processDefinitionKey: Long?
) {
    constructor() : this("", 0, "", 0, 0, null, null, null)
}
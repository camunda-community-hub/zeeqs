package io.zeebe.zeeqs.data.entity

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id


@Entity
class ElementInstanceStateTransition(
    @Id val partitionIdWithPosition: String,
    val elementInstanceKey: Long,
    @Enumerated(EnumType.STRING)
    val state: ElementInstanceState,
    val timestamp: Long
) {
    constructor() : this("", 0, ElementInstanceState.ACTIVATING, 0)
}
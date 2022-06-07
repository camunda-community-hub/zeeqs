package io.zeebe.zeeqs.data.entity

import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Enumerated
import javax.persistence.EnumType

@Entity
class ElementInstanceStateTransition(
        @Id val partitionIdWithPosition: String,
        val elementInstanceKey: Long,
        @Enumerated(EnumType.STRING)
        val state: ElementInstanceState,
        val timestamp: Long
)
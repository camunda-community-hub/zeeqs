package io.zeebe.zeeqs.data.entity

import javax.persistence.Entity
import javax.persistence.Id

@Entity
class ElementInstanceStateTransition(
        @Id val position: Long,
        val elementInstanceKey: Long,
        val state: ElementInstanceState,
        val timestamp: Long
)
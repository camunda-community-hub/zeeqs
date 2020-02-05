package io.zeebe.zeeqs.data.entity

import javax.persistence.Entity
import javax.persistence.Id

@Entity
class Variable(
        @Id val key: Long,
        val name: String,
        val workflowInstanceKey: Long,
        val scopeKey: Long,
        var value: String,
        var timestamp: Long
)
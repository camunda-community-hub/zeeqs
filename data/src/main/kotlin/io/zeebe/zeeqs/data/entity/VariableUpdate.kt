package io.zeebe.zeeqs.data.entity

import javax.persistence.Entity
import javax.persistence.Id

@Entity
class VariableUpdate(
        @Id val position: Long,
        val variableKey: Long,
        val name: String,
        val value: String,
        val workflowInstanceKey: Long,
        val scopeKey: Long,
        val timestamp: Long
)
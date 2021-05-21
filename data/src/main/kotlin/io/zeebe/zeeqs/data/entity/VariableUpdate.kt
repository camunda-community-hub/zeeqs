package io.zeebe.zeeqs.data.entity

import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Lob

@Entity
class VariableUpdate(
    @Id val position: Long,
    val variableKey: Long,
    val name: String,
    @Lob val value: String,
    val processInstanceKey: Long,
    val scopeKey: Long,
    val timestamp: Long
)
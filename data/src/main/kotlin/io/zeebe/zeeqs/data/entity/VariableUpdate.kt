package io.zeebe.zeeqs.data.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Lob

@Entity
class VariableUpdate(
    @Id val partitionIdWithPosition: String,
    val variableKey: Long,
    val name: String,
    @Lob @Column(name = "value_") val value: String,
    val processInstanceKey: Long,
    val scopeKey: Long,
    val timestamp: Long
) {
    constructor() : this("", 0, "", "", 0, 0, 0)
}
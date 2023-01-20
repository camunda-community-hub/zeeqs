package io.zeebe.zeeqs.data.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Lob

@Entity
class VariableUpdate(
        @Id val partitionIdWithPosition: String,
        val variableKey: Long,
        val name: String,
        @Lob @Column(name = "value_") val value: String,
        val processInstanceKey: Long,
        val scopeKey: Long,
        val timestamp: Long
)
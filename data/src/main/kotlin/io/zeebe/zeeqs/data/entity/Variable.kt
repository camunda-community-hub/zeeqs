package io.zeebe.zeeqs.data.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Lob


@Entity
class Variable(
    @Id @Column(name = "key_") val key: Long,
    val position: Long,
    val name: String,
    val processInstanceKey: Long,
    val processDefinitionKey: Long,
    val scopeKey: Long,
    @Lob @Column(name = "value_") var value: String,
    var timestamp: Long
) {
    constructor() : this(0, 0, "", 0, 0, 0L, "", 0)
}
package io.zeebe.zeeqs.data.entity

import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Lob

@Entity
class Variable(
    @Id val key: Long,
    val name: String,
    val processInstanceKey: Long,
    val scopeKey: Long,
    @Lob var value: String,
    var timestamp: Long
)
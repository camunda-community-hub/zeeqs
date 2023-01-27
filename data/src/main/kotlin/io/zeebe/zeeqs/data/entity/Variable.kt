package io.zeebe.zeeqs.data.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Lob

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
)
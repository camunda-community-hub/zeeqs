package io.zeebe.zeeqs.data.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Lob

@Entity
class MessageVariable(
        @Id val id: String,
        val name: String,
        @Lob @Column(name = "value_") val value: String,
        val messageKey: Long,
        val position: Long
)
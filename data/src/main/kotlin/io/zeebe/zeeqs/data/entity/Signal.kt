package io.zeebe.zeeqs.data.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

@Entity
class Signal(
    @Id @Column(name = "key_") val key: Long,
    val position: Long,
    val name: String,
    var timestamp: Long
)
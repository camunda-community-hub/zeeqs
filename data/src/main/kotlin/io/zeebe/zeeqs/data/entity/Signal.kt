package io.zeebe.zeeqs.data.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
class Signal(
    @Id @Column(name = "key_") val key: Long,
    val position: Long,
    val name: String,
    var timestamp: Long
) {
    constructor() : this(0, 0, "", 0)
}
package io.zeebe.zeeqs.data.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Lob


@Entity
class SignalVariable(
    @Id val id: String,
    val name: String,
    @Lob @Column(name = "value_") val value: String,
    val signalKey: Long,
    val position: Long
) {
    constructor() : this("", "", "", 0, 0)
}
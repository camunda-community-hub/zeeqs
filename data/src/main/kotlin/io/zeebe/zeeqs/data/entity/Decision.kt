package io.zeebe.zeeqs.data.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
data class Decision(
    @Id @Column(name = "key_") val key: Long,
    val decisionId: String,
    val decisionName: String,
    val version: Int,
    val decisionRequirementsKey: Long,
    val decisionRequirementsId: String
) {
    constructor() : this(0, "", "", 0, 0, "")
}
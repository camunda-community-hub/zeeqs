package io.zeebe.zeeqs.data.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Lob

@Entity
data class DecisionRequirements(
    @Id @Column(name = "key_") val key: Long,
    val decisionRequirementsId: String,
    val decisionRequirementsName: String,
    val version: Int,
    val namespace: String,
    @Lob val dmnXML: String,
    val deployTime: Long,
    val resourceName: String,
    @Lob val checksum: String
) {
    constructor() : this(0, "", "", 0, "", "", 0, "", "")
}
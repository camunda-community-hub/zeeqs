package io.zeebe.zeeqs.data.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Lob

@Entity
data class DecisionRequirements(
    @Id @Column(name = "key_") val key: Long,
    val id: String,
    val name: String,
    val version: Int,
    val namespace: String,
    @Lob val dmnXml: String,
    val deployTime: Long,
    val resourceName: String,
    @Lob val checksum: String
)
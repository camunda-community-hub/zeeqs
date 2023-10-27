package io.zeebe.zeeqs.data.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Lob


@Entity
data class Process(
    @Id @Column(name = "key_") val key: Long,
    val bpmnProcessId: String,
    val version: Int,
    @Lob val bpmnXML: String,
    val deployTime: Long,
    val resourceName: String,
    @Lob val checksum: String
) {
    constructor() : this(0, "", 0, "", 0, "", "")
}

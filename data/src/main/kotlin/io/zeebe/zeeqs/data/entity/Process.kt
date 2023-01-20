package io.zeebe.zeeqs.data.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Lob

@Entity
data class Process(
        @Id @Column(name = "key_") val key: Long,
        val bpmnProcessId: String,
        val version: Int,
        @Lob val bpmnXML: String,
        val deployTime: Long,
        val resourceName: String,
        @Lob val checksum: String)

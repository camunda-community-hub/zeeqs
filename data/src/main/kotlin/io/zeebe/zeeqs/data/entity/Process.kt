package io.zeebe.zeeqs.data.entity

import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Lob

@Entity
data class Process(
        @Id val key: Long,
        val bpmnProcessId: String,
        val version: Int,
        @Lob val bpmnXML: String,
        val deployTime: Long,
        val resourceName: String,
        @Lob val checksum: String)

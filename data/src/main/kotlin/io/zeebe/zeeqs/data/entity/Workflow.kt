package io.zeebe.zeeqs.data.entity

import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Lob

@Entity
data class Workflow(
        @Id val key: Long,
        val bpmnProcessId: String,
        val version: Int,
        @Lob val bpmnXML: String,
        val timestamp: Long) {

}

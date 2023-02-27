package io.zeebe.zeeqs.data.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class Decision(
        @Id @Column(name = "key_") val key: Long,
        val decisionId: String,
        val decisionName: String,
        val version: Int,
        val decisionRequirementsKey: Long,
        val decisionRequirementsId: String
)
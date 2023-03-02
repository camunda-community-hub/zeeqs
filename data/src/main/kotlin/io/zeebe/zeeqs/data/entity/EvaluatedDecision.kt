package io.zeebe.zeeqs.data.entity

import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Lob

@Entity
data class EvaluatedDecision(
    @Id val id: String,
    val decisionKey: Long,
    @Lob val decisionOutput: String,
    val decisionEvaluationKey: Long
)

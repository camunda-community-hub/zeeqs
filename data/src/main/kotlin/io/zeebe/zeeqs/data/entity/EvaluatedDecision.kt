package io.zeebe.zeeqs.data.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Lob

@Entity
data class EvaluatedDecision(
    @Id val id: String,
    val decisionKey: Long,
    @Lob val decisionOutput: String,
    val decisionEvaluationKey: Long
) {
    constructor() : this("", 0, "", 0)
}

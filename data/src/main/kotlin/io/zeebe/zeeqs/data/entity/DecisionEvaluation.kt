package io.zeebe.zeeqs.data.entity

import javax.persistence.*

@Entity
data class DecisionEvaluation(
    @Id @Column(name = "key_") val key: Long,
    val decisionKey: Long,
    @Lob val decisionOutput: String,
    @Enumerated(EnumType.STRING) var state: DecisionEvaluationState = DecisionEvaluationState.EVALUATED,
    val evaluationTime: Long,
    val failedDecisionId: String? = null,
    @Lob val evaluationFailureMessage: String? = null,
    val processInstanceKey: Long? = null,
    val elementInstanceKey: Long? = null
)

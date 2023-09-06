package io.zeebe.zeeqs.data.entity

import jakarta.persistence.*

@Entity
data class DecisionEvaluation(
    @Id @Column(name = "key_") val key: Long,
    val decisionKey: Long,
    val decisionRequirementsKey: Long,
    @Lob val decisionOutput: String,
    @Enumerated(EnumType.STRING) var state: DecisionEvaluationState = DecisionEvaluationState.EVALUATED,
    val evaluationTime: Long,
    val failedDecisionId: String? = null,
    @Lob val evaluationFailureMessage: String? = null,
    val processInstanceKey: Long? = null,
    val elementInstanceKey: Long? = null
) {
    constructor() : this(0, 0, 0, "", DecisionEvaluationState.EVALUATED, 0, null, null, null, null)
}

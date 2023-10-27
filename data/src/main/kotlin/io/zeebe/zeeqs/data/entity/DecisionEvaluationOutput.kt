package io.zeebe.zeeqs.data.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Lob

@Entity
data class DecisionEvaluationOutput(
    @Id val id: String,
    val outputId: String,
    val outputName: String,
    @Lob @Column(name = "value_") val value: String,
    val evaluatedDecisionId: String,
    val ruleId: String,
    val ruleIndex: Int
) {
    constructor() : this("", "", "", "", "", "", 0)
}

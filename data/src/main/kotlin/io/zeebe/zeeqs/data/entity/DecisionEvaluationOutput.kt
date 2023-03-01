package io.zeebe.zeeqs.data.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Lob

@Entity
data class DecisionEvaluationOutput(
    @Id val id: String,
    val outputId: String,
    val outputName: String,
    @Lob @Column(name = "value_") val value: String,
    val decisionEvaluationKey: Long,
    val ruleId: String,
    val ruleIndex: Int
)

package io.zeebe.zeeqs.data.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Lob

@Entity
data class DecisionEvaluationInput(
    @Id val id: String,
    val inputId: String,
    val inputName: String,
    @Lob @Column(name = "value_") val value: String,
    val evaluatedDecisionId: String
)

package io.zeebe.zeeqs.data.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Lob

@Entity
data class DecisionEvaluationInput(
    @Id val id: String,
    val inputId: String,
    val inputName: String,
    @Lob @Column(name = "value_") val value: String,
    val evaluatedDecisionId: String
) {
    constructor() : this("", "", "", "", "")
}

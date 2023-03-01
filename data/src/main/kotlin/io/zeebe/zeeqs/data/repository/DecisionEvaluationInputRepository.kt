package io.zeebe.zeeqs.data.repository

import io.zeebe.zeeqs.data.entity.DecisionEvaluationInput
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface DecisionEvaluationInputRepository :
    PagingAndSortingRepository<DecisionEvaluationInput, String> {

    fun findAllByEvaluatedDecisionId(evaluatedDecisionId: String): List<DecisionEvaluationInput>
}
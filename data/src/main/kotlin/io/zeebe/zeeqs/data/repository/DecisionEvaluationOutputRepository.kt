package io.zeebe.zeeqs.data.repository

import io.zeebe.zeeqs.data.entity.DecisionEvaluationOutput
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface DecisionEvaluationOutputRepository :
    PagingAndSortingRepository<DecisionEvaluationOutput, String> {

    fun findAllByEvaluatedDecisionId(evaluatedDecisionId: String): List<DecisionEvaluationOutput>
}
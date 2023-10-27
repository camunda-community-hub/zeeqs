package io.zeebe.zeeqs.data.repository

import io.zeebe.zeeqs.data.entity.DecisionEvaluationOutput
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface DecisionEvaluationOutputRepository :
    PagingAndSortingRepository<DecisionEvaluationOutput, String>, CrudRepository<DecisionEvaluationOutput, String> {

    fun findAllByEvaluatedDecisionId(evaluatedDecisionId: String): List<DecisionEvaluationOutput>
}
package io.zeebe.zeeqs.data.repository

import io.zeebe.zeeqs.data.entity.EvaluatedDecision
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface EvaluatedDecisionRepository :
    PagingAndSortingRepository<EvaluatedDecision, String> {

    fun findAllByDecisionEvaluationKey(decisionEvaluationKey: Long): List<EvaluatedDecision>
}
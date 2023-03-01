package io.zeebe.zeeqs.data.repository

import io.zeebe.zeeqs.data.entity.DecisionEvaluation
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface DecisionEvaluationRepository : PagingAndSortingRepository<DecisionEvaluation, Long> {

    fun findAllByDecisionKey(decisionKey: Long): List<DecisionEvaluation>

    fun findAllByProcessInstanceKey(processInstanceKey: Long): List<DecisionEvaluation>

    fun findAllByElementInstanceKey(elementInstanceKey: Long): List<DecisionEvaluation>
}
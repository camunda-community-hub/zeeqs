package io.zeebe.zeeqs.data.repository

import io.zeebe.zeeqs.data.entity.DecisionEvaluation
import io.zeebe.zeeqs.data.entity.DecisionEvaluationState
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface DecisionEvaluationRepository : PagingAndSortingRepository<DecisionEvaluation, Long> {

    fun findAllByDecisionKey(decisionKey: Long): List<DecisionEvaluation>

    fun countByDecisionKey(decisionKey: Long): Long

    fun findAllByProcessInstanceKey(processInstanceKey: Long): List<DecisionEvaluation>

    fun countByProcessInstanceKey(processInstanceKey: Long): Long

    fun findAllByElementInstanceKey(elementInstanceKey: Long): List<DecisionEvaluation>

    fun countByElementInstanceKey(elementInstanceKey: Long): Long

    fun findByStateIn(
        stateIn: List<DecisionEvaluationState>,
        pageable: Pageable
    ): List<DecisionEvaluation>

    fun countByStateIn(stateIn: List<DecisionEvaluationState>): Long
}
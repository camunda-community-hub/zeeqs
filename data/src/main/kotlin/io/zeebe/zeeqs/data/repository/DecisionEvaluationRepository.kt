package io.zeebe.zeeqs.data.repository

import io.zeebe.zeeqs.data.entity.DecisionEvaluation
import io.zeebe.zeeqs.data.entity.DecisionEvaluationState
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface DecisionEvaluationRepository : PagingAndSortingRepository<DecisionEvaluation, Long> {

    fun findAllByDecisionKey(
        decisionKey: Long,
        pageable: Pageable
    ): List<DecisionEvaluation>

    fun findAllByDecisionKeyAndStateIn(
        decisionKey: Long,
        stateIn: List<DecisionEvaluationState>,
        pageable: Pageable
    ): List<DecisionEvaluation>

    fun countByDecisionKeyAndStateIn(
        decisionKey: Long,
        stateIn: List<DecisionEvaluationState>
    ): Long

    fun countByDecisionKey(decisionKey: Long): Long

    fun findAllByProcessInstanceKeyAndStateIn(
        processInstanceKey: Long,
        stateIn: List<DecisionEvaluationState>,
        pageable: Pageable
    ): List<DecisionEvaluation>

    fun countByProcessInstanceKeyAndStateIn(
        processInstanceKey: Long,
        stateIn: List<DecisionEvaluationState>
    ): Long

    fun findAllByElementInstanceKey(elementInstanceKey: Long): List<DecisionEvaluation>

    fun countByElementInstanceKey(elementInstanceKey: Long): Long

    fun findByStateIn(
        stateIn: List<DecisionEvaluationState>,
        pageable: Pageable
    ): List<DecisionEvaluation>

    fun countByStateIn(stateIn: List<DecisionEvaluationState>): Long
}
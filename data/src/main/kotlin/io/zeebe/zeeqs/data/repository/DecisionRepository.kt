package io.zeebe.zeeqs.data.repository

import io.zeebe.zeeqs.data.entity.Decision
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface DecisionRepository : PagingAndSortingRepository<Decision, Long> {

    fun findAllByDecisionRequirementsKey(decisionRequirementsKey: Long): List<Decision>

    fun findByDecisionRequirementsKeyAndDecisionId(
        decisionRequirementsKey: Long,
        decisionId: String
    ): Decision?
}
package io.zeebe.zeeqs.data.repository

import io.zeebe.zeeqs.data.entity.ElementInstanceStateTransition
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface ElementInstanceStateTransitionRepository : PagingAndSortingRepository<ElementInstanceStateTransition, String>,
    CrudRepository<ElementInstanceStateTransition, String> {

    fun findByElementInstanceKey(elementInstanceKey: Long): List<ElementInstanceStateTransition>
}
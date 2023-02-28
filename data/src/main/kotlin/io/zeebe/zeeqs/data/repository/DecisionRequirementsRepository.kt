package io.zeebe.zeeqs.data.repository

import io.zeebe.zeeqs.data.entity.DecisionRequirements
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface DecisionRequirementsRepository : PagingAndSortingRepository<DecisionRequirements, Long>
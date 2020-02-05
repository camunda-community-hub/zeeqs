package io.zeebe.zeeqs.data.repository

import io.zeebe.zeeqs.data.entity.Workflow
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface WorkflowRepository : PagingAndSortingRepository<Workflow, Long>
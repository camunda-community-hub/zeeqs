package io.zeebe.zeeqs.data.repository

import io.zeebe.zeeqs.data.entity.Process
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface ProcessRepository : PagingAndSortingRepository<Process, Long>
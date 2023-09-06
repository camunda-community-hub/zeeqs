package io.zeebe.zeeqs.data.repository

import io.zeebe.zeeqs.data.entity.Process
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface ProcessRepository : JpaRepository<Process, Long>
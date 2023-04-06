package io.zeebe.zeeqs.data.repository

import io.zeebe.zeeqs.data.entity.Signal
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface SignalRepository : PagingAndSortingRepository<Signal, Long> {

}
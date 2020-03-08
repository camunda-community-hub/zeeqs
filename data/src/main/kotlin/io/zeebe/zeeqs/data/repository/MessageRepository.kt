package io.zeebe.zeeqs.data.repository

import io.zeebe.zeeqs.data.entity.Message
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface MessageRepository : PagingAndSortingRepository<Message, Long> {


}
package io.zeebe.zeeqs.data.resolvers

import com.coxautodev.graphql.tools.GraphQLQueryResolver
import io.zeebe.zeeqs.data.entity.Job
import io.zeebe.zeeqs.data.repository.JobRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class JobQueryResolver(
        val jobRepository: JobRepository
) : GraphQLQueryResolver {

    fun jobs(count: Int, offset: Int): List<Job> {
        return jobRepository.findAll(PageRequest.of(offset, count)).toList()
    }

    fun job(key: Long): Job? {
        return jobRepository.findByIdOrNull(key)
    }

}
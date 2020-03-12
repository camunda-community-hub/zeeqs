package io.zeebe.zeeqs.data.resolvers

import com.coxautodev.graphql.tools.GraphQLQueryResolver
import io.zeebe.zeeqs.data.entity.Job
import io.zeebe.zeeqs.data.entity.JobState
import io.zeebe.zeeqs.data.repository.JobRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class JobQueryResolver(
        val jobRepository: JobRepository
) : GraphQLQueryResolver {

    fun jobs(
            count: Int,
            offset: Int,
            stateIn: List<JobState>,
            jobTypeIn: List<String>): List<Job> {

        return if (jobTypeIn.isEmpty()) {
            jobRepository.findByStateIn(stateIn, PageRequest.of(offset, count)).toList()
        } else {
            jobRepository.findByStateInAndJobTypeIn(
                    stateIn = stateIn,
                    jobTypeIn = jobTypeIn,
                    pageable = PageRequest.of(offset, count)
            )
        }
    }

    fun job(key: Long): Job? {
        return jobRepository.findByIdOrNull(key)
    }

}
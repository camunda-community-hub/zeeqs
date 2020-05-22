package io.zeebe.zeeqs.data.resolvers

import graphql.kickstart.tools.GraphQLQueryResolver
import io.zeebe.zeeqs.data.entity.Job
import io.zeebe.zeeqs.data.entity.JobState
import io.zeebe.zeeqs.data.repository.JobRepository
import io.zeebe.zeeqs.graphql.resolvers.connection.JobConnection
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class JobQueryResolver(
        val jobRepository: JobRepository
) : GraphQLQueryResolver {

    fun jobs(
            limit: Int,
            page: Int,
            stateIn: List<JobState>,
            jobTypeIn: List<String>): JobConnection {

        if (jobTypeIn.isEmpty()) {
            return JobConnection(
                    getItems = { jobRepository.findByStateIn(stateIn, PageRequest.of(page, limit)).toList() },
                    getCount = { jobRepository.countByStateIn(stateIn) }
            )
        } else {
            return JobConnection(
                    getItems = {
                        jobRepository.findByStateInAndJobTypeIn(
                                stateIn = stateIn,
                                jobTypeIn = jobTypeIn,
                                pageable = PageRequest.of(page, limit)
                        )
                    },
                    getCount = {
                        jobRepository.countByStateInAndJobTypeIn(
                                stateIn = stateIn,
                                jobTypeIn = jobTypeIn)
                    }
            )
        }
    }

    fun job(key: Long): Job? {
        return jobRepository.findByIdOrNull(key)
    }

}
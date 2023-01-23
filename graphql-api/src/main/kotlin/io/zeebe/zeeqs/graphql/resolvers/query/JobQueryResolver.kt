package io.zeebe.zeeqs.graphql.resolvers.query

import io.zeebe.zeeqs.data.entity.Job
import io.zeebe.zeeqs.data.entity.JobState
import io.zeebe.zeeqs.data.repository.JobRepository
import io.zeebe.zeeqs.graphql.resolvers.connection.JobConnection
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class JobQueryResolver(
        val jobRepository: JobRepository
) {

    @QueryMapping
    fun jobs(
            @Argument perPage: Int,
            @Argument page: Int,
            @Argument stateIn: List<JobState>,
            @Argument jobTypeIn: List<String>): JobConnection {

        if (jobTypeIn.isEmpty()) {
            return JobConnection(
                    getItems = { jobRepository.findByStateIn(stateIn, PageRequest.of(page, perPage)).toList() },
                    getCount = { jobRepository.countByStateIn(stateIn) }
            )
        } else {
            return JobConnection(
                    getItems = {
                        jobRepository.findByStateInAndJobTypeIn(
                                stateIn = stateIn,
                                jobTypeIn = jobTypeIn,
                                pageable = PageRequest.of(page, perPage)
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

    @QueryMapping
    fun job(@Argument key: Long): Job? {
        return jobRepository.findByIdOrNull(key)
    }

}
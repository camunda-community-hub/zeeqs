package io.zeebe.zeeqs.graphql.resolvers.type

import com.coxautodev.graphql.tools.GraphQLResolver
import io.zeebe.zeeqs.data.entity.Job
import org.springframework.stereotype.Component

@Component
class JobListResolver : GraphQLResolver<JobList> {

    fun items(list: JobList): List<Job> {
        return list.getItems()
    }

    fun totalCount(list: JobList): Long {
        return list.getCount()
    }

}
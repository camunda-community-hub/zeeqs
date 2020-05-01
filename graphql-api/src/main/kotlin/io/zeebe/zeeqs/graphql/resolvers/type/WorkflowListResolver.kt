package io.zeebe.zeeqs.graphql.resolvers.type

import com.coxautodev.graphql.tools.GraphQLResolver
import io.zeebe.zeeqs.data.entity.Workflow
import org.springframework.stereotype.Component

@Component
class WorkflowListResolver : GraphQLResolver<WorkflowList> {

    fun items(list: WorkflowList): List<Workflow> {
        return list.getItems()
    }

    fun totalCount(list: WorkflowList): Long {
        return list.getCount()
    }

}
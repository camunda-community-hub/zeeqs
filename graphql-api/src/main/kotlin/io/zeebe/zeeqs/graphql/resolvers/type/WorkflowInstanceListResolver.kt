package io.zeebe.zeeqs.graphql.resolvers.type

import com.coxautodev.graphql.tools.GraphQLResolver
import io.zeebe.zeeqs.data.entity.WorkflowInstance
import org.springframework.stereotype.Component

@Component
class WorkflowInstanceListResolver : GraphQLResolver<WorkflowInstanceList> {

    fun items(list: WorkflowInstanceList): List<WorkflowInstance> {
        return list.getItems()
    }

    fun totalCount(list: WorkflowInstanceList): Long {
        return list.getCount()
    }

}
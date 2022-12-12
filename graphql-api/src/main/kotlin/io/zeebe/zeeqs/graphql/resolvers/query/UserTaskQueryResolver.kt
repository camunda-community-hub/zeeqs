package io.zeebe.zeeqs.graphql.resolvers.query

import graphql.kickstart.tools.GraphQLQueryResolver
import io.zeebe.zeeqs.data.entity.UserTask
import io.zeebe.zeeqs.data.entity.UserTaskState
import io.zeebe.zeeqs.data.repository.UserTaskRepository
import io.zeebe.zeeqs.graphql.resolvers.connection.UserTaskConnection
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class UserTaskQueryResolver(
        val userTaskRepository: UserTaskRepository
) : GraphQLQueryResolver {

    fun userTasks(
            perPage: Int,
            page: Int,
            stateIn: List<UserTaskState>): UserTaskConnection {

        return UserTaskConnection(
                getItems = {
                    userTaskRepository.findByStateIn(
                            stateIn = stateIn,
                            pageable = PageRequest.of(page, perPage)
                    )
                },
                getCount = {
                    userTaskRepository.countByStateIn(
                            stateIn = stateIn
                    )
                }
        )
    }

    fun userTask(key: Long): UserTask? {
        return userTaskRepository.findByIdOrNull(key)
    }

}
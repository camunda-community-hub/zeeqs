package io.zeebe.zeeqs.graphql.resolvers.query

import io.zeebe.zeeqs.data.entity.UserTask
import io.zeebe.zeeqs.data.entity.UserTaskState
import io.zeebe.zeeqs.data.repository.UserTaskRepository
import io.zeebe.zeeqs.graphql.resolvers.connection.UserTaskConnection
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class UserTaskQueryResolver(
        val userTaskRepository: UserTaskRepository
) {

    @QueryMapping
    fun userTasks(
            @Argument perPage: Int,
            @Argument page: Int,
            @Argument stateIn: List<UserTaskState>
    ): UserTaskConnection {

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

    @QueryMapping
    fun userTask(@Argument key: Long): UserTask? {
        return userTaskRepository.findByIdOrNull(key)
    }

}
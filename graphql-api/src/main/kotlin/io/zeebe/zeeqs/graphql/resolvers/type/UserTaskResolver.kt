package io.zeebe.zeeqs.graphql.resolvers.type

import graphql.kickstart.tools.GraphQLResolver
import io.zeebe.zeeqs.data.entity.ElementInstance
import io.zeebe.zeeqs.data.entity.ProcessInstance
import io.zeebe.zeeqs.data.entity.UserTask
import io.zeebe.zeeqs.data.repository.ElementInstanceRepository
import io.zeebe.zeeqs.data.repository.ProcessInstanceRepository
import io.zeebe.zeeqs.data.repository.UserTaskRepository
import io.zeebe.zeeqs.data.service.ProcessService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class UserTaskResolver(
        val processInstanceRepository: ProcessInstanceRepository,
        val elementInstanceRepository: ElementInstanceRepository,
        val processService: ProcessService
) : GraphQLResolver<UserTask> {

    fun timestamp(userTask: UserTask, zoneId: String): String? {
        return userTask.timestamp.let { ResolverExtension.timestampToString(it, zoneId) }
    }

    fun startTime(userTask: UserTask, zoneId: String): String? {
        return userTask.startTime?.let { ResolverExtension.timestampToString(it, zoneId) }
    }

    fun endTime(userTask: UserTask, zoneId: String): String? {
        return userTask.endTime?.let { ResolverExtension.timestampToString(it, zoneId) }
    }

    fun processInstance(userTask: UserTask): ProcessInstance? {
        return processInstanceRepository.findByIdOrNull(userTask.processInstanceKey)
    }

    fun elementInstance(userTask: UserTask): ElementInstance? {
        return elementInstanceRepository.findByIdOrNull(userTask.elementInstanceKey)
    }

    fun form(userTask: UserTask): UserTaskForm? {
        return userTask.formKey?.let { formKey ->
            UserTaskForm(
                    key = formKey,
                    resource = formKey
                            .takeIf { userTask.isCamundaForm }
                            ?.let {
                                processService.getForm(
                                        processDefinitionKey = userTask.processDefinitionKey,
                                        formKey = formKey
                                )
                            }
            )
        }
    }

}
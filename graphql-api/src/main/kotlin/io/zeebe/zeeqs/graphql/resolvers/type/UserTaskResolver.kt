package io.zeebe.zeeqs.graphql.resolvers.type

import io.zeebe.zeeqs.data.entity.ElementInstance
import io.zeebe.zeeqs.data.entity.ProcessInstance
import io.zeebe.zeeqs.data.entity.UserTask
import io.zeebe.zeeqs.data.repository.ElementInstanceRepository
import io.zeebe.zeeqs.data.repository.ProcessInstanceRepository
import io.zeebe.zeeqs.data.service.ProcessService
import io.zeebe.zeeqs.data.service.UserTaskForm
import org.springframework.data.repository.findByIdOrNull
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class UserTaskResolver(
        val processInstanceRepository: ProcessInstanceRepository,
        val elementInstanceRepository: ElementInstanceRepository,
        val processService: ProcessService
) {

    @SchemaMapping(typeName = "UserTask", field = "timestamp")
    fun timestamp(
            userTask: UserTask,
            @Argument zoneId: String
    ): String? {
        return userTask.timestamp.let { ResolverExtension.timestampToString(it, zoneId) }
    }

    @SchemaMapping(typeName = "UserTask", field = "startTime")
    fun startTime(
            userTask: UserTask,
            @Argument zoneId: String
    ): String? {
        return userTask.startTime?.let { ResolverExtension.timestampToString(it, zoneId) }
    }

    @SchemaMapping(typeName = "UserTask", field = "endTime")
    fun endTime(
            userTask: UserTask,
            @Argument zoneId: String
    ): String? {
        return userTask.endTime?.let { ResolverExtension.timestampToString(it, zoneId) }
    }

    @SchemaMapping(typeName = "UserTask", field = "processInstance")
    fun processInstance(userTask: UserTask): ProcessInstance? {
        return processInstanceRepository.findByIdOrNull(userTask.processInstanceKey)
    }

    @SchemaMapping(typeName = "UserTask", field = "elementInstance")
    fun elementInstance(userTask: UserTask): ElementInstance? {
        return elementInstanceRepository.findByIdOrNull(userTask.elementInstanceKey)
    }

    @SchemaMapping(typeName = "UserTask", field = "form")
    fun form(userTask: UserTask): UserTaskForm? {
        return userTask.formKey?.let { formKey ->
            UserTaskForm(
                    key = formKey,
                    resource = processService.getForm(
                            processDefinitionKey = userTask.processDefinitionKey,
                            formKey = formKey
                    )
            )
        }
    }

}
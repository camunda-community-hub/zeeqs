package io.zeebe.zeeqs.data.entity

import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.Id

@Entity
data class UserTask(
        @Id val key: Long,
        val position: Long,
        val processInstanceKey: Long,
        val processDefinitionKey: Long,
        val elementInstanceKey: Long,
        val assignee: String?,
        val candidateGroups: String?,
        val formKey: String?
) {
    @Enumerated(EnumType.STRING)
    var state: UserTaskState = UserTaskState.CREATED
    var timestamp: Long = -1

    var startTime: Long? = null
    var endTime: Long? = null

}

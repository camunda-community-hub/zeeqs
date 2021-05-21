package io.zeebe.zeeqs.data.entity

import javax.persistence.Entity
import javax.persistence.Enumerated
import javax.persistence.EnumType
import javax.persistence.Id

@Entity
data class ProcessIntance(
    @Id val key: Long,
    val bpmnProcessId: String,
    val version: Int,
    val processDefinitionKey: Long,
    val parentProcessInstanceKey: Long?,
    val parentElementInstanceKey: Long?) {

    @Enumerated(EnumType.STRING)
    var state: ProcessInstanceState = ProcessInstanceState.ACTIVATED

    var startTime: Long? = null
    var endTime: Long? = null
}

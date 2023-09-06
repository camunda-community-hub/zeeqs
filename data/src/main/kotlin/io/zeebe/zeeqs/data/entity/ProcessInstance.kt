package io.zeebe.zeeqs.data.entity

import jakarta.persistence.*

@Entity
data class ProcessInstance(
    @Id @Column(name = "key_") val key: Long,
    val position: Long,
    val bpmnProcessId: String,
    val version: Int,
    val processDefinitionKey: Long,
    val parentProcessInstanceKey: Long?,
    val parentElementInstanceKey: Long?
) {
    constructor() : this(0, 0, "", 0, 0, null, null)

    @Enumerated(EnumType.STRING)
    var state: ProcessInstanceState = ProcessInstanceState.ACTIVATED

    var startTime: Long? = null
    var endTime: Long? = null
}

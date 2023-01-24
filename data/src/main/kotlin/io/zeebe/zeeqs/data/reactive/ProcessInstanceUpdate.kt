package io.zeebe.zeeqs.data.reactive

data class ProcessInstanceUpdate(
        val processInstanceKey: Long,
        val processKey: Long,
        val updateType: ProcessInstanceUpdateType
)
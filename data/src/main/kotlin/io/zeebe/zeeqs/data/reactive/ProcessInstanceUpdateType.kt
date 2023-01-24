package io.zeebe.zeeqs.data.reactive

enum class ProcessInstanceUpdateType {
    PROCESS_INSTANCE_STATE,
    ELEMENT_INSTANCE,
    VARIABLE,
    INCIDENT,
    JOB
}
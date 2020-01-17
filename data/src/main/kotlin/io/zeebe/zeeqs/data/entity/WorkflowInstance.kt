package io.zeebe.zeeqs.data.entity

import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class WorkflowInstance(
        @Id var key: Long,
        var bpmnProcessId: String,
        var version: Int,
        var workflowKey: Long) {

    var state: WorkflowInstanceState = WorkflowInstanceState.ACTIVATED

    @Transient
    var variables: List<Variable> = ArrayList()
}

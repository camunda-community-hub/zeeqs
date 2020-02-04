package io.zeebe.zeeqs.data.entity

import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class WorkflowInstance(
        @Id val key: Long,
        val bpmnProcessId: String,
        val version: Int,
        val workflowKey: Long,
        val parentWorkflowInstanceKey: Long?,
        val parentElementInstanceKey: Long?) {

    var state: WorkflowInstanceState = WorkflowInstanceState.ACTIVATED

    var startTime: Long? = null
    var endTime: Long? = null

    @Transient
    var workflow: Workflow? = null

    @Transient
    var variables: List<Variable> = ArrayList()

    @Transient
    var jobs: List<Job> = ArrayList()
}

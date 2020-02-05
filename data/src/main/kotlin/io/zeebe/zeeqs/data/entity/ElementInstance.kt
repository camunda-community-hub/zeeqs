package io.zeebe.zeeqs.data.entity

import javax.persistence.Entity
import javax.persistence.Id

@Entity
class ElementInstance(
        @Id val key: Long,
        val elementId: String,
        val bpmnElementType: BpmnElementType,
        val workflowInstanceKey: Long,
        val workflowKey: Long,
        val scopeKey: Long?
) {

    var state: ElementInstanceState = ElementInstanceState.ACTIVATING

    var startTime: Long? = null
    var endTime: Long? = null

}
package io.zeebe.zeeqs.data.entity

import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Enumerated
import javax.persistence.EnumType

@Entity
class ElementInstance(
        @Id val key: Long,
        val elementId: String,
        @Enumerated(EnumType.STRING)
        val bpmnElementType: BpmnElementType,
        val workflowInstanceKey: Long,
        val workflowKey: Long,
        val scopeKey: Long?
) {

    
    var state: ElementInstanceState = ElementInstanceState.ACTIVATING

    var startTime: Long? = null
    var endTime: Long? = null

}
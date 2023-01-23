package io.zeebe.zeeqs.data.entity

import javax.persistence.*

@Entity
class ElementInstance(
        @Id @Column(name = "key_") val key: Long,
        val position: Long,
        val elementId: String,
        @Enumerated(EnumType.STRING)
        val bpmnElementType: BpmnElementType,
        val processInstanceKey: Long,
        val processDefinitionKey: Long,
        val scopeKey: Long?
) {


    var state: ElementInstanceState = ElementInstanceState.ACTIVATING

    var startTime: Long? = null
    var endTime: Long? = null

}
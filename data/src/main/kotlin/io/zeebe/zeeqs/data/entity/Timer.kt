package io.zeebe.zeeqs.data.entity

import javax.persistence.Entity
import javax.persistence.Enumerated
import javax.persistence.EnumType
import javax.persistence.Id

@Entity
class Timer(
    @Id val key: Long,
    val position: Long,
    val dueDate: Long,
    var repetitions: Int,
    val elementId: String,
    var processInstanceKey: Long?,
    val elementInstanceKey: Long?,
    val processDefinitionKey: Long?
) {

    @Enumerated(EnumType.STRING)
    var state: TimerState = TimerState.CREATED

    var startTime: Long? = null
    var endTime: Long? = null

}
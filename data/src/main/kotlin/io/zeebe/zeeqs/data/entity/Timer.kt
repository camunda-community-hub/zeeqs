package io.zeebe.zeeqs.data.entity

import javax.persistence.Entity
import javax.persistence.Id

@Entity
class Timer(
        @Id val key: Long,
        val dueDate: Long,
        var repetitions: Int,
        val workflowInstanceKey: Long?,
        val elementInstanceKey: Long?,
        val workflowKey: Long?
) {

    var state: TimerState = TimerState.CREATED
    var timestamp: Long = -1

}
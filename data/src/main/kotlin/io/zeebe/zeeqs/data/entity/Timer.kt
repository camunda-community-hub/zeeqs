package io.zeebe.zeeqs.data.entity

import javax.persistence.Entity
import javax.persistence.Enumerated
import javax.persistence.EnumType
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

    @Enumerated(EnumType.STRING)      
    var state: TimerState = TimerState.CREATED

    var startTime: Long? = null
    var endTime: Long? = null

}
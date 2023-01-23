package io.zeebe.zeeqs.data.entity

import javax.persistence.*

@Entity
class Timer(
        @Id @Column(name = "key_") val key: Long,
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
package io.zeebe.zeeqs.data.entity

import javax.persistence.Entity
import javax.persistence.Enumerated
import javax.persistence.EnumType
import javax.persistence.Id

@Entity
class Job(
        @Id val key: Long,
        val jobType: String,
        val workflowInstanceKey: Long,
        val elementInstanceKey: Long
) {

    @Enumerated(EnumType.STRING)
    var state: JobState = JobState.ACTIVATABLE
    var timestamp: Long = -1

    var startTime: Long? = null
    var endTime: Long? = null

    var retries: Int? = null
    var worker: String? = null

}
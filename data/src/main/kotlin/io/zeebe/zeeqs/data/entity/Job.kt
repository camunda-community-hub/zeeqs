package io.zeebe.zeeqs.data.entity

import javax.persistence.Entity
import javax.persistence.Id

@Entity
class Job(
        @Id val key: Long,
        val jobType: String,
        val workflowInstanceKey: Long,
        val elementInstanceKey: Long
) {

    var state: JobState = JobState.ACTIVATABLE
    var timestamp: Long = -1

    var retries: Int? = null
    var worker: String? = null

}
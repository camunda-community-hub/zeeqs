package io.zeebe.zeeqs.data.entity

import javax.persistence.Entity
import javax.persistence.Id

@Entity
class Incident(
        @Id val key: Long,
        val errorType: String,
        val errorMessage: String,
        val workflowInstanceKey: Long,
        val elementInstanceKey: Long,
        val jobKey: Long?
) {

    var state: IncidentState = IncidentState.CREATED
    var creationTime: Long? = null
    var resolveTime: Long? = null

}
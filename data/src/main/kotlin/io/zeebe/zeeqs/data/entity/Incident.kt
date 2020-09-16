package io.zeebe.zeeqs.data.entity

import javax.persistence.Entity
import javax.persistence.Enumerated
import javax.persistence.EnumType
import javax.persistence.Id
import javax.persistence.Lob

@Entity
class Incident(
        @Id val key: Long,
        val errorType: String,
        @Lob val errorMessage: String,
        val workflowInstanceKey: Long,
        val elementInstanceKey: Long,
        val jobKey: Long?
) {

    @Enumerated(EnumType.STRING)
    var state: IncidentState = IncidentState.CREATED
    var creationTime: Long? = null
    var resolveTime: Long? = null

}
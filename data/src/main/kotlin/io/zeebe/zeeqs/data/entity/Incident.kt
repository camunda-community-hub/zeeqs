package io.zeebe.zeeqs.data.entity

import jakarta.persistence.*


@Entity
class Incident(
    @Id @Column(name = "key_") val key: Long,
    val position: Long,
    val errorType: String,
    @Lob val errorMessage: String,
    val processInstanceKey: Long,
    val elementInstanceKey: Long,
    val processDefinitionKey: Long,
    val jobKey: Long?
) {
    constructor() : this(0, 0, "", "", 0, 0, 0, null)

    @Enumerated(EnumType.STRING)
    var state: IncidentState = IncidentState.CREATED
    var creationTime: Long? = null
    var resolveTime: Long? = null

}
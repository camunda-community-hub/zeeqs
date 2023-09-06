package io.zeebe.zeeqs.data.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Lob


@Entity
class Error(
    @Id val position: Long,
    val errorEventPosition: Long,
    @Lob val exceptionMessage: String,
    @Lob val stacktrace: String,
    val processInstanceKey: Long?
) {
    constructor() : this(0, 0, "", "", null)
}
package io.zeebe.zeeqs.data.entity

import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Lob

@Entity
class Error(@Id val position: Long,
            val errorEventPosition: Long,
            @Lob val exceptionMessage: String,
            @Lob val stacktrace:String,
            val processInstanceKey: Long?
            )
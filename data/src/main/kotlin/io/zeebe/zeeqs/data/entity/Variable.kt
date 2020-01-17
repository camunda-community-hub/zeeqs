package io.zeebe.zeeqs.data.entity

import javax.persistence.Entity
import javax.persistence.Id

@Entity
class Variable(
        @Id var key: Long,
        var name: String,
        var value: String,
        var workflowInstanceKey: Long)
package io.zeebe.zeeqs.importer.hazelcast

import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
data class HazelcastConfig(
    @Id val id: String,
    var sequence: Long
) {
        constructor() : this("", 0)
}
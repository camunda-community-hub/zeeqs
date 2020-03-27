package io.zeebe.zeeqs.importer.hazelcast

import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class HazelcastConfig(
        @Id val id: String,
        var sequence: Long)
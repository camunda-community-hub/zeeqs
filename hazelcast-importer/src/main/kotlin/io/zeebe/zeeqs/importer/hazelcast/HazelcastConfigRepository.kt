package io.zeebe.zeeqs.importer.hazelcast

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface HazelcastConfigRepository : CrudRepository<HazelcastConfig, String> {

}
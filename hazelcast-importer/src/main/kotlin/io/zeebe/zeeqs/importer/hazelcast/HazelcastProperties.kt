package io.zeebe.zeeqs.importer.hazelcast

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

//@ConstructorBinding
@ConfigurationProperties("zeebe.client.worker.hazelcast")
data class HazelcastProperties(
        val connection: String = "localhost:5701",
        val connectionTimeout: String = "PT1M",
        val ringbuffer: String = "zeebe",
        val connectionInitialBackoff: String = "PT15S",
        val connectionBackoffMultiplier: Double = 2.0,
        val connectionMaxBackoff: String = "PT30S"
)
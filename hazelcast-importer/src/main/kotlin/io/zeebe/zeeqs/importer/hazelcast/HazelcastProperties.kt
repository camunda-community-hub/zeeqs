package io.zeebe.zeeqs.importer.hazelcast

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("zeebe.client.worker.hazelcast")
data class HazelcastProperties(
        val connection: String = "localhost:5701",
        val connectionTimeout: String = "PT30S",
        val ringbuffer: String = "zeebe",
        val connectionInitialBackoff: Int = 15 * 1000,
        val connectionBackoffMultiplier: Double = 2.0,
        val connectionMaxBackoff: Int = 60 * 1000
)
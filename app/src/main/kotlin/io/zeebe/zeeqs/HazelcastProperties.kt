package io.zeebe.zeeqs

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("zeebe.hazelcast")
data class HazelcastProperties(
        val connection: String = "localhost:5701",
        val connectionTimeout: String = "PT30S"
)
package io.zeebe.zeeqs

import io.zeebe.zeeqs.importer.hazelcast.HazelcastImporter
import io.zeebe.zeeqs.importer.hazelcast.HazelcastProperties
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import javax.annotation.PostConstruct

@SpringBootApplication
@EnableCaching
@EnableConfigurationProperties(HazelcastProperties::class)
class ZeeqlApplication(
        val hazelcastProperties: HazelcastProperties,
        val hazelcastImporter: HazelcastImporter
) {
    val logger = LoggerFactory.getLogger(ZeeqlApplication::class.java)

    @PostConstruct
    fun init() {
        logger.info("Connecting to Hazelcast: '$hazelcastProperties'")
        hazelcastImporter.start(hazelcastProperties)
        logger.info("Connected to Hazelcast!")
    }
}

fun main(args: Array<String>) {
    runApplication<ZeeqlApplication>(*args)
}
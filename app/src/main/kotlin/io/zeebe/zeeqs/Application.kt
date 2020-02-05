package io.zeebe.zeeqs

import io.zeebe.zeeqs.importer.hazelcast.HazelcastImporter
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import javax.annotation.PostConstruct

@SpringBootApplication
@EnableCaching
class ZeeqlApplication(
        val importer: HazelcastImporter
) {

    @PostConstruct
    fun init() {
        println("importing from Hazelcast")
        importer.start("localhost:5701")
    }
}

fun main(args: Array<String>) {
    runApplication<ZeeqlApplication>(*args)
}

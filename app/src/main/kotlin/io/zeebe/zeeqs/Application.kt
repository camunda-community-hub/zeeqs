package io.zeebe.zeeqs

import io.zeebe.zeeqs.importer.hazelcast.HazelcastImporter
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import javax.annotation.PostConstruct

@SpringBootApplication
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

package io.zeebe.zeeqs

import io.zeebe.zeeqs.importer.hazelcast.HazelcastImporter
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import java.time.Duration
import javax.annotation.PostConstruct

@SpringBootApplication
@EnableCaching
class ZeeqlApplication() {

}

fun main(args: Array<String>) {
    runApplication<ZeeqlApplication>(*args)
}

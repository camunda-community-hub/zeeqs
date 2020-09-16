package io.zeebe.zeeqs

import io.zeebe.zeeqs.importer.hazelcast.HazelcastImporter
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import java.time.Duration
import javax.annotation.PostConstruct
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Import
import io.zeebe.exporter.source.hazelcast.HazelcastSourceConfiguration

@ConditionalOnProperty(name=["zeeqs.hazelcast.enabled"], havingValue="true")
@Import(HazelcastSourceConfiguration::class)
class ZeeqlHazelcastConfiguration {
  
}

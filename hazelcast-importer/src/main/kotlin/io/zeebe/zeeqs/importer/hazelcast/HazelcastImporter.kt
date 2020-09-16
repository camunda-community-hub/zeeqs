package io.zeebe.zeeqs.importer.hazelcast

import com.hazelcast.client.HazelcastClient
import com.hazelcast.client.config.ClientConfig
import io.zeebe.exporter.proto.Schema
import io.zeebe.hazelcast.connect.java.ZeebeHazelcast
import io.zeebe.zeeqs.data.entity.*
import io.zeebe.zeeqs.data.repository.*
import org.springframework.stereotype.Component
import java.time.Duration
import io.zeebe.zeeqs.importer.protobuf.ProtobufImporter
import java.util.function.Consumer
import io.zeebe.zeeqs.importer.protobuf.ProtobufSource
import java.util.Optional
import io.zeebe.exporter.source.hazelcast.HazelcastSourceConnector
import io.zeebe.exporter.source.hazelcast.HazelcastSource

@Component
class HazelcastImporter(val hazelcastConfigRepository: HazelcastConfigRepository) : HazelcastSourceConnector {


  fun hazelcastConfig() : HazelcastConfig {
    var iter = hazelcastConfigRepository.findAll().iterator()
    if (iter.hasNext()) return iter.next()
    return (HazelcastConfig(
      id = "cfg",
      sequence = -1))
  }

  override fun startPosition() : Optional<Long> {
    var cfg = hazelcastConfig();
    if (cfg.sequence < 0) return Optional.empty()
    return Optional.of(cfg.sequence)
  }

  override fun connectTo(source: HazelcastSource) {
    // most connection code is in ProtobufImporter, the code here is just for storing the sequence 
    val updateSequence: ((Long) -> Unit) = {
      var cfg = hazelcastConfig()
      cfg.sequence = it
      hazelcastConfigRepository.save(cfg)
    }

    source.postProcessListener(updateSequence)

  }

          
}
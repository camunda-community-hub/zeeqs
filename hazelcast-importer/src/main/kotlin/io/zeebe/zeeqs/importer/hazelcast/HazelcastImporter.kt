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


@Component
class HazelcastImporter(
        val hazelcastConfigRepository: HazelcastConfigRepository,
        val workflowRepository: WorkflowRepository,
        val protobufImporter: ProtobufImporter) {

    var zeebeHazelcast: ZeebeHazelcast? = null

    fun start(hazelcastConnection: String, hazelcastConnectionTimeout: Duration) {

        val hazelcastConfig = hazelcastConfigRepository.findById(hazelcastConnection)
                .orElse(HazelcastConfig(
                        id = hazelcastConnection,
                        sequence = -1))

        val updateSequence: ((Long) -> Unit) = {
            hazelcastConfig.sequence = it
            hazelcastConfigRepository.save(hazelcastConfig)
        }

        val clientConfig = ClientConfig()
        val networkConfig = clientConfig.networkConfig
        networkConfig.addresses = listOf(hazelcastConnection)

        val connectionRetryConfig = clientConfig.connectionStrategyConfig.connectionRetryConfig
        connectionRetryConfig.clusterConnectTimeoutMillis = hazelcastConnectionTimeout.toMillis()

        val hazelcast = HazelcastClient.newHazelcastClient(clientConfig)

        val builder = ZeebeHazelcast.newBuilder(hazelcast)
                .addDeploymentListener { it.takeIf { it.metadata.key > 0 }?.let(protobufImporter::importDeploymentRecord) }
                .addWorkflowInstanceListener { it.takeIf { it.metadata.key > 0 }?.let(protobufImporter::importWorkflowInstanceRecord) }
                .addVariableListener { it.takeIf { it.metadata.key > 0 }?.let(protobufImporter::importVariableRecord) }
                .addJobListener { it.takeIf { it.metadata.key > 0 }?.let(protobufImporter::importJobRecord) }
                .addIncidentListener { it.takeIf { it.metadata.key > 0 }?.let(protobufImporter::importIncidentRecord) }
                .addTimerListener { it.takeIf { it.metadata.key > 0 }?.let(protobufImporter::importTimerRecord) }
                .addMessageListener { it.takeIf { it.metadata.key > 0 }?.let(protobufImporter::importMessageRecord) }
                .addMessageSubscriptionListener(protobufImporter::importMessageSubscriptionRecord)
                .addMessageStartEventSubscriptionListener(protobufImporter::importMessageStartEventSubscriptionRecord)
                .addWorkflowInstanceSubscriptionListener { it.takeIf { it.metadata.key > 0 }?.let(protobufImporter::importWorkflowInstanceSubscriptionRecord) }
                .postProcessListener(updateSequence)

        if (hazelcastConfig.sequence >= 0) {
            builder.readFrom(hazelcastConfig.sequence)
        } else {
            builder.readFromHead()
        }

        zeebeHazelcast = builder.build()
    }

    fun stop() {
        zeebeHazelcast?.close()
    }
}

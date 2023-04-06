package io.zeebe.zeeqs.importer.hazelcast

import io.zeebe.exporter.proto.Schema
import io.zeebe.exporter.proto.Schema.SignalSubscriptionRecord
import io.zeebe.zeeqs.data.entity.Signal
import io.zeebe.zeeqs.data.entity.SignalSubscription
import io.zeebe.zeeqs.data.entity.SignalSubscriptionState
import io.zeebe.zeeqs.data.entity.SignalVariable
import io.zeebe.zeeqs.data.repository.SignalRepository
import io.zeebe.zeeqs.data.repository.SignalSubscriptionRepository
import io.zeebe.zeeqs.data.repository.SignalVariableRepository
import io.zeebe.zeeqs.importer.hazelcast.ProtobufTransformer.structToMap
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class HazelcastSignalImporter(
    private val signalRepository: SignalRepository,
    private val signalSubscriptionRepository: SignalSubscriptionRepository,
    private val signalVariableRepository: SignalVariableRepository
) {

    fun importSignal(signal: Schema.SignalRecord) {
        val entity = signalRepository.findByIdOrNull(signal.metadata.key)
            ?: createSignal(signal)

        signalRepository.save(entity)

        importSignalVariables(signal)
    }

    private fun createSignal(signal: Schema.SignalRecord): Signal {
        return Signal(
            key = signal.metadata.key,
            position = signal.metadata.position,
            name = signal.signalName,
            timestamp = signal.metadata.timestamp
        )
    }

    private fun importSignalVariables(signal: Schema.SignalRecord) {
        val signalKey = signal.metadata.key

        val entities = structToMap(signal.variables).map {
            val variableName = it.key

            SignalVariable(
                id = "$signalKey-$variableName",
                name = variableName,
                value = it.value,
                signalKey = signalKey,
                position = signal.metadata.position
            )
        }

        signalVariableRepository.saveAll(entities)
    }

    fun importSignalSubscription(signalSubscription: Schema.SignalSubscriptionRecord) {
        val entity = signalSubscriptionRepository.findByIdOrNull(signalSubscription.metadata.key)
            ?: createSignalSubscription(signalSubscription)

        when (signalSubscription.metadata.intent) {
            "CREATED" -> entity.state = SignalSubscriptionState.CREATED
            "DELETED" -> entity.state = SignalSubscriptionState.DELETED
        }

        entity.timestamp = signalSubscription.metadata.timestamp

        signalSubscriptionRepository.save(entity)
    }

    private fun createSignalSubscription(signalSubscription: SignalSubscriptionRecord): SignalSubscription {
        return SignalSubscription(
            key = signalSubscription.metadata.key,
            position = signalSubscription.metadata.position,
            signalName = signalSubscription.signalName,
            processDefinitionKey = signalSubscription.processDefinitionKey,
            elementId = signalSubscription.catchEventId
        )
    }

}
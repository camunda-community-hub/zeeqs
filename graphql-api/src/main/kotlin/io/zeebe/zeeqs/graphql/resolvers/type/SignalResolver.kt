package io.zeebe.zeeqs.graphql.resolvers.type

import io.zeebe.zeeqs.data.entity.Signal
import io.zeebe.zeeqs.data.entity.SignalVariable
import io.zeebe.zeeqs.data.repository.SignalVariableRepository
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class SignalResolver(
    val signalVariableRepository: SignalVariableRepository
) {

    @SchemaMapping(typeName = "Signal", field = "timestamp")
    fun timestamp(
        signal: Signal,
        @Argument zoneId: String
    ): String? {
        return signal.timestamp.let { ResolverExtension.timestampToString(it, zoneId) }
    }

    @SchemaMapping(typeName = "Signal", field = "variables")
    fun variables(signal: Signal): List<SignalVariable> {
        return signalVariableRepository.findBySignalKey(signalKey = signal.key)
    }

}
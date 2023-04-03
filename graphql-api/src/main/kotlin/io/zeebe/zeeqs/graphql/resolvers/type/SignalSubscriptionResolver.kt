package io.zeebe.zeeqs.graphql.resolvers.type

import io.zeebe.zeeqs.data.entity.Process
import io.zeebe.zeeqs.data.entity.SignalSubscription
import io.zeebe.zeeqs.data.repository.ProcessRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class SignalSubscriptionResolver(
    val processRepository: ProcessRepository
) {

    @SchemaMapping(typeName = "SignalSubscription", field = "timestamp")
    fun timestamp(
        signalSubscription: SignalSubscription,
        @Argument zoneId: String
    ): String? {
        return signalSubscription.timestamp.let { ResolverExtension.timestampToString(it, zoneId) }
    }

    @SchemaMapping(typeName = "SignalSubscription", field = "process")
    fun process(signalSubscription: SignalSubscription): Process? {
        return processRepository.findByIdOrNull(id = signalSubscription.processDefinitionKey)
    }

    @SchemaMapping(typeName = "SignalSubscription", field = "element")
    fun element(signalSubscription: SignalSubscription): BpmnElement? {
        return BpmnElement(
            processDefinitionKey = signalSubscription.processDefinitionKey,
            elementId = signalSubscription.elementId
        )
    }

}
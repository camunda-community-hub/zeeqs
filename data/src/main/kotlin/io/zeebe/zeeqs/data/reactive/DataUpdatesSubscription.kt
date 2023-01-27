package io.zeebe.zeeqs.data.reactive

import io.zeebe.zeeqs.data.entity.Process
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux

@Component
class DataUpdatesSubscription(private val publisher: DataUpdatesPublisher) {

    fun processSubscription(): Flux<Process> {
        return Flux.create { sink ->
            publisher.registerProcessListener { sink.next(it) }
        }
    }

    fun processInstanceUpdateSubscription(): Flux<ProcessInstanceUpdate> {
        return Flux.create { sink ->
            publisher.registerProcessInstanceListener {
                sink.next(ProcessInstanceUpdate(
                        processInstanceKey = it.key,
                        processKey = it.processDefinitionKey,
                        updateType = ProcessInstanceUpdateType.PROCESS_INSTANCE_STATE
                ))
            }

            publisher.registerElementInstanceListener {
                sink.next(ProcessInstanceUpdate(
                        processInstanceKey = it.processInstanceKey,
                        processKey = it.processDefinitionKey,
                        updateType = ProcessInstanceUpdateType.ELEMENT_INSTANCE
                ))
            }

            publisher.registerVariableListener {
                sink.next(ProcessInstanceUpdate(
                        processInstanceKey = it.processInstanceKey,
                        processKey = it.processDefinitionKey,
                        updateType = ProcessInstanceUpdateType.VARIABLE
                ))
            }

            publisher.registerIncidentListener {
                sink.next(ProcessInstanceUpdate(
                        processInstanceKey = it.processInstanceKey,
                        processKey = it.processDefinitionKey,
                        updateType = ProcessInstanceUpdateType.INCIDENT
                ))
            }

            publisher.registerJobListener {
                sink.next(ProcessInstanceUpdate(
                        processInstanceKey = it.processInstanceKey,
                        processKey = it.processDefinitionKey,
                        updateType = ProcessInstanceUpdateType.JOB
                ))
            }
        }
    }

}
package io.zeebe.zeeqs

import io.camunda.zeebe.model.bpmn.Bpmn
import io.zeebe.zeeqs.data.entity.Decision
import io.zeebe.zeeqs.data.entity.Process
import io.zeebe.zeeqs.data.entity.ProcessInstance
import io.zeebe.zeeqs.data.reactive.DataUpdatesSubscription
import io.zeebe.zeeqs.data.reactive.ProcessInstanceUpdate
import io.zeebe.zeeqs.data.reactive.ProcessInstanceUpdateType
import io.zeebe.zeeqs.data.repository.ProcessInstanceRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.graphql.test.tester.GraphQlTester
import org.springframework.graphql.test.tester.WebSocketGraphQlTester
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import java.net.URI
import java.time.Instant
import java.util.*


@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["spring.graphql.websocket.path=/graphql"]
)
@TestConfiguration
class ZeebeGraphqlSubscriptionTest(
    @LocalServerPort val port: Int
) {

    @MockBean
    private lateinit var dataUpdatesSubscription: DataUpdatesSubscription

    @MockBean
    private lateinit var processInstanceRepository: ProcessInstanceRepository

    private val bpmnProcess = Bpmn.createExecutableProcess("process")
        .startEvent("start")
        .endEvent("end")
        .done()

    fun graphqlTester(): GraphQlTester {
        val uri = URI("http://localhost:$port/graphql")
        val client = ReactorNettyWebSocketClient()

        return WebSocketGraphQlTester.create(uri, client)
    }

    @BeforeEach
    fun `mock process instance repository`() {
        Mockito.`when`(processInstanceRepository.findByIdOrNull(Mockito.anyLong())).thenAnswer {
            Optional.of(
                ProcessInstance(
                    key = it.arguments[0] as Long,
                    position = 1,
                    bpmnProcessId = "process",
                    version = 1,
                    processDefinitionKey = 10,
                    parentProcessInstanceKey = null,
                    parentElementInstanceKey = null
                )
            )
        }
    }

    @Test
    fun `should subscribe to process updates`() {
        // given
        Mockito.`when`(dataUpdatesSubscription.processSubscription())
            .thenReturn(
                Flux.just(
                    process(key = 10),
                    process(key = 20)
                )
            )

        // when
        val flux = graphqlTester().document(
            """
                    subscription {
                      processUpdates {
                        key
                      }
                    }
                    """
        )
            .executeSubscription()
            .toFlux()

        // then
        StepVerifier.create(flux)
            .consumeNextWith {
                it.path("processUpdates").matchesJson(
                    """
                    {
                        "key": "10"
                    }
                    """
                )
            }
            .consumeNextWith {
                it.path("processUpdates").matchesJson(
                    """
                    {
                        "key": "20"
                    }
                    """
                )
            }
            .verifyComplete();
    }

    @Test
    fun `should subscribe to all process instance updates`() {
        // given
        Mockito.`when`(dataUpdatesSubscription.processInstanceUpdateSubscription())
            .thenReturn(
                Flux.just(
                    ProcessInstanceUpdate(
                        processKey = 1,
                        processInstanceKey = 10,
                        updateType = ProcessInstanceUpdateType.PROCESS_INSTANCE_STATE
                    ),
                    ProcessInstanceUpdate(
                        processKey = 2,
                        processInstanceKey = 20,
                        updateType = ProcessInstanceUpdateType.PROCESS_INSTANCE_STATE
                    ),
                    ProcessInstanceUpdate(
                        processKey = 1,
                        processInstanceKey = 10,
                        updateType = ProcessInstanceUpdateType.VARIABLE
                    )
                )
            )

        // when
        val flux = graphqlTester().document(
            """
                    subscription {
                      processInstanceUpdates {
                        processInstance {
                            key
                        }
                        updateType
                      }
                    }
                    """
        )
            .executeSubscription()
            .toFlux()

        // then
        StepVerifier.create(flux)
            .consumeNextWith {
                it.path("processInstanceUpdates").matchesJson(
                    """
                    {
                        "processInstance": {
                            "key": "10"
                        },
                        "updateType": "PROCESS_INSTANCE_STATE"
                    }
                    """
                )
            }
            .consumeNextWith {
                it.path("processInstanceUpdates").matchesJson(
                    """
                    {
                        "processInstance": {
                            "key": "20"
                        },
                        "updateType": "PROCESS_INSTANCE_STATE"
                    }
                    """
                )
            }
            .consumeNextWith {
                it.path("processInstanceUpdates").matchesJson(
                    """
                    {
                        "processInstance": {
                            "key": "10"
                        },
                        "updateType": "VARIABLE"
                    }
                    """
                )
            }
            .verifyComplete();
    }

    @Test
    fun `should subscribe to process instance updates of given process`() {
        // given
        Mockito.`when`(dataUpdatesSubscription.processInstanceUpdateSubscription())
            .thenReturn(
                Flux.just(
                    ProcessInstanceUpdate(
                        processKey = 1,
                        processInstanceKey = 10,
                        updateType = ProcessInstanceUpdateType.PROCESS_INSTANCE_STATE
                    ),
                    ProcessInstanceUpdate(
                        processKey = 2,
                        processInstanceKey = 20,
                        updateType = ProcessInstanceUpdateType.PROCESS_INSTANCE_STATE
                    ),
                    ProcessInstanceUpdate(
                        processKey = 1,
                        processInstanceKey = 11,
                        updateType = ProcessInstanceUpdateType.VARIABLE
                    )
                )
            )

        // when
        val flux = graphqlTester().document(
            """
                    subscription {
                      processInstanceUpdates(filter: {processKey: 1}) {
                        processInstance {
                            key
                        }
                        updateType
                      }
                    }
                    """
        )
            .executeSubscription()
            .toFlux()

        // then
        StepVerifier.create(flux)
            .consumeNextWith {
                it.path("processInstanceUpdates").matchesJson(
                    """
                    {
                        "processInstance": {
                            "key": "10"
                        },
                        "updateType": "PROCESS_INSTANCE_STATE"
                    }
                    """
                )
            }
            .consumeNextWith {
                it.path("processInstanceUpdates").matchesJson(
                    """
                    {
                        "processInstance": {
                            "key": "11"
                        },
                        "updateType": "VARIABLE"
                    }
                    """
                )
            }
            .verifyComplete();
    }

    @Test
    fun `should subscribe to process instance updates of given instance`() {
        // given
        Mockito.`when`(dataUpdatesSubscription.processInstanceUpdateSubscription())
            .thenReturn(
                Flux.just(
                    ProcessInstanceUpdate(
                        processKey = 1,
                        processInstanceKey = 10,
                        updateType = ProcessInstanceUpdateType.PROCESS_INSTANCE_STATE
                    ),
                    ProcessInstanceUpdate(
                        processKey = 2,
                        processInstanceKey = 20,
                        updateType = ProcessInstanceUpdateType.PROCESS_INSTANCE_STATE
                    ),
                    ProcessInstanceUpdate(
                        processKey = 1,
                        processInstanceKey = 10,
                        updateType = ProcessInstanceUpdateType.VARIABLE
                    )
                )
            )

        // when
        val flux = graphqlTester().document(
            """
                    subscription {
                      processInstanceUpdates(filter: {processInstanceKey: 10}) {
                        processInstance {
                            key
                        }
                        updateType
                      }
                    }
                    """
        )
            .executeSubscription()
            .toFlux()

        // then
        StepVerifier.create(flux)
            .consumeNextWith {
                it.path("processInstanceUpdates").matchesJson(
                    """
                    {
                        "processInstance": {
                            "key": "10"
                        },
                        "updateType": "PROCESS_INSTANCE_STATE"
                    }
                    """
                )
            }
            .consumeNextWith {
                it.path("processInstanceUpdates").matchesJson(
                    """
                    {
                        "processInstance": {
                            "key": "10"
                        },
                        "updateType": "VARIABLE"
                    }
                    """
                )
            }
            .verifyComplete();
    }

    @Test
    fun `should subscribe to process instance updates of given update type`() {
        // given
        Mockito.`when`(dataUpdatesSubscription.processInstanceUpdateSubscription())
            .thenReturn(
                Flux.just(
                    ProcessInstanceUpdate(
                        processKey = 1,
                        processInstanceKey = 10,
                        updateType = ProcessInstanceUpdateType.PROCESS_INSTANCE_STATE
                    ),
                    ProcessInstanceUpdate(
                        processKey = 1,
                        processInstanceKey = 10,
                        updateType = ProcessInstanceUpdateType.VARIABLE
                    ),
                    ProcessInstanceUpdate(
                        processKey = 2,
                        processInstanceKey = 20,
                        updateType = ProcessInstanceUpdateType.PROCESS_INSTANCE_STATE
                    )
                )
            )

        // when
        val flux = graphqlTester().document(
            """
                    subscription {
                      processInstanceUpdates(filter: {updateTypeIn: [PROCESS_INSTANCE_STATE]}) {
                        processInstance {
                            key
                        }
                        updateType
                      }
                    }
                    """
        )
            .executeSubscription()
            .toFlux()

        // then
        StepVerifier.create(flux)
            .consumeNextWith {
                it.path("processInstanceUpdates").matchesJson(
                    """
                    {
                        "processInstance": {
                            "key": "10"
                        },
                        "updateType": "PROCESS_INSTANCE_STATE"
                    }
                    """
                )
            }
            .consumeNextWith {
                it.path("processInstanceUpdates").matchesJson(
                    """
                    {
                        "processInstance": {
                            "key": "20"
                        },
                        "updateType": "PROCESS_INSTANCE_STATE"
                    }
                    """
                )
            }
            .verifyComplete();
    }

    @Test
    fun `should subscribe to decision updates`() {
        // given
        Mockito.`when`(dataUpdatesSubscription.decisionSubscription())
            .thenReturn(
                Flux.just(
                    decision(key = 10),
                    decision(key = 20)
                )
            )

        // when
        val flux = graphqlTester().document(
            """
                    subscription {
                      decisionUpdates {
                        key
                      }
                    }
                    """
        )
            .executeSubscription()
            .toFlux()

        // then
        StepVerifier.create(flux)
            .consumeNextWith {
                it.path("decisionUpdates").matchesJson(
                    """
                    {
                        "key": "10"
                    }
                    """
                )
            }
            .consumeNextWith {
                it.path("decisionUpdates").matchesJson(
                    """
                    {
                        "key": "20"
                    }
                    """
                )
            }
            .verifyComplete();
    }

    private fun process(key: Long): Process {
        return Process(
            key = key,
            bpmnProcessId = "process",
            version = 1,
            bpmnXML = Bpmn.convertToString(bpmnProcess),
            deployTime = Instant.now().toEpochMilli(),
            resourceName = "process.bpmn",
            checksum = "checksum"
        )
    }

    private fun decision(key: Long): Decision {
        return Decision(
            key = key,
            decisionId = "decision-id",
            decisionName = "decision-name",
            version = 1,
            decisionRequirementsKey = 10,
            decisionRequirementsId = "decision-requirements-id"
        )
    }

    @SpringBootApplication
    class TestConfig

}
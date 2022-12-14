package io.zeebe.zeeqs

import io.camunda.zeebe.model.bpmn.Bpmn
import io.zeebe.zeeqs.data.entity.*
import io.zeebe.zeeqs.data.repository.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.core.env.Environment
import org.springframework.core.env.get
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Instant

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestConfiguration
class ZeebeGraphqlVariableTest(
        @LocalServerPort private val port: Int,
        @Autowired val variableRepository: VariableRepository,
        @Autowired val elementInstanceRepository: ElementInstanceRepository,
        @Autowired val processInstanceRepository: ProcessInstanceRepository) {

    private val graphqlAssertions = GraphqlAssertions(port = port)

    private val processInstanceKey = 10L
    private val scopeKey = 20L

    @BeforeEach
    fun `create variables`() {
        variableRepository.save(
                Variable(
                        name = "x",
                        value = "global",
                        scopeKey = processInstanceKey,
                        processInstanceKey = processInstanceKey,
                        key = 1L,
                        position = 2L,
                        timestamp = 1L
                )
        )

        variableRepository.save(
                Variable(
                        name = "x",
                        value = "local",
                        scopeKey = scopeKey,
                        processInstanceKey = processInstanceKey,
                        key = 2L,
                        position = 2L,
                        timestamp = 1L
                )
        )
    }

    @BeforeEach
    fun `create scopes`() {
        elementInstanceRepository.save(ElementInstance(
                key = processInstanceKey,
                position = 1,
                elementId = "",
                bpmnElementType = BpmnElementType.UNSPECIFIED,
                processInstanceKey = processInstanceKey,
                processDefinitionKey = 1L,
                scopeKey = null
        ))

        elementInstanceRepository.save(ElementInstance(
                key = scopeKey,
                position = 1,
                elementId = "",
                bpmnElementType = BpmnElementType.UNSPECIFIED,
                processInstanceKey = processInstanceKey,
                processDefinitionKey = 1L,
                scopeKey = processInstanceKey
        ))
    }

    @BeforeEach
    fun `create process instance`() {
        processInstanceRepository.save(ProcessInstance(
                key = processInstanceKey,
                position = 1,
                bpmnProcessId = "",
                version = 1,
                processDefinitionKey = 1L,
                parentProcessInstanceKey = null,
                parentElementInstanceKey = null
        ))
    }

    @Test
    fun `should get local variables`() {
        // when/then
        graphqlAssertions.assertQuery(
                query = """
                    {
                      processInstance(key: $processInstanceKey) {
                        elementInstances {      
                          key
                          variables {
                            name
                            value
                          }
                        }
                      }
                    } 
                    """,
                expectedResponseBody = """
                    {
                      "data": {
                        "processInstance": {
                          "elementInstances": [
                            {
                              "key": "$processInstanceKey",
                              "variables": [
                                {
                                  "name": "x",
                                  "value": "global"
                                }
                              ]
                            },
                            {
                              "key": "$scopeKey",
                              "variables": [
                                {
                                  "name": "x",
                                  "value": "local"
                                }
                              ]
                            }
                          ]
                        }
                      }
                    }
                    """)
    }

    @Test
    fun `should get all variables with shadowing`() {
        // when/then
        graphqlAssertions.assertQuery(
                query = """
                    {
                      processInstance(key: $processInstanceKey) {
                        elementInstances {      
                          key
                          variables(localOnly: false) {
                            name
                            value
                          }
                        }
                      }
                    } 
                    """,
                expectedResponseBody = """
                    {
                      "data": {
                        "processInstance": {
                          "elementInstances": [
                            {
                              "key": "$processInstanceKey",
                              "variables": [
                                {
                                  "name": "x",
                                  "value": "global"
                                }
                              ]
                            },
                            {
                              "key": "$scopeKey",
                              "variables": [
                                {
                                  "name": "x",
                                  "value": "local"
                                }
                              ]
                            }
                          ]
                        }
                      }
                    }
                    """)
    }

    @Test
    fun `should get all variables`() {
        // when/then
        graphqlAssertions.assertQuery(
                query = """
                    {
                      processInstance(key: $processInstanceKey) {
                        elementInstances {      
                          key
                          variables(localOnly: false, shadowing: false) {
                            name
                            value
                          }
                        }
                      }
                    } 
                    """,
                expectedResponseBody = """
                    {
                      "data": {
                        "processInstance": {
                          "elementInstances": [
                            {
                              "key": "$processInstanceKey",
                              "variables": [
                                {
                                  "name": "x",
                                  "value": "global"
                                }
                              ]
                            },
                            {
                              "key": "$scopeKey",
                              "variables": [
                                {
                                  "name": "x",
                                  "value": "global"
                                },
                                {
                                  "name": "x",
                                  "value": "local"
                                }
                              ]
                            }
                          ]
                        }
                      }
                    }
                    """)
    }

    @SpringBootApplication
    class TestConfiguration

}
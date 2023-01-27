package io.zeebe.zeeqs

import io.zeebe.zeeqs.data.entity.BpmnElementType
import io.zeebe.zeeqs.data.entity.ElementInstance
import io.zeebe.zeeqs.data.entity.ProcessInstance
import io.zeebe.zeeqs.data.entity.Variable
import io.zeebe.zeeqs.data.repository.ElementInstanceRepository
import io.zeebe.zeeqs.data.repository.ProcessInstanceRepository
import io.zeebe.zeeqs.data.repository.VariableRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.graphql.test.tester.GraphQlTester

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestConfiguration
class ZeebeGraphqlVariableTest(
        @Autowired private val graphQlTester: GraphQlTester,
        @Autowired val variableRepository: VariableRepository,
        @Autowired val elementInstanceRepository: ElementInstanceRepository,
        @Autowired val processInstanceRepository: ProcessInstanceRepository) {

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
                        processDefinitionKey = 10L,
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
                        processDefinitionKey = 10L,
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
        graphQlTester.document("""
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
                    """)
                .execute()
                .path("processInstance.elementInstances")
                .matchesJson("""
                    [
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
                    """)
    }

    @Test
    fun `should get all variables of element instance with shadowing`() {
        // when/then
        graphQlTester.document("""
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
                    """)
                .execute()
                .path("processInstance.elementInstances")
                .matchesJson("""
                    [
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
                    """)
    }

    @Test
    fun `should get all variables of element instance`() {
        // when/then
        graphQlTester.document("""
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
                    """)
                .execute()
                .path("processInstance.elementInstances")
                .matchesJson("""
                    [
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
                    """)
    }

    @Test
    fun `should get all variables of process instance`() {
        // when/then
        graphQlTester.document("""
                    {
                      processInstance(key: $processInstanceKey) {
                        variables {
                          name
                          value
                        }
                      }
                    } 
                    """)
                .execute()
                .path("processInstance.variables")
                .matchesJson("""
                    [
                        {
                          "name": "x",
                          "value": "global"
                        },
                        {
                          "name": "x",
                          "value": "local"
                        }
                    ]        
                    """)
    }

    @Test
    fun `should get all global variables`() {
        // when/then
        graphQlTester.document("""
                    {
                      processInstance(key: $processInstanceKey) {
                        variables(globalOnly: true) {
                          name
                          value
                        }
                      }
                    } 
                    """)
                .execute()
                .path("processInstance.variables")
                .matchesJson("""
                    [
                        {
                          "name": "x",
                          "value": "global"
                        }
                    ]      
                    """)
    }

    @SpringBootApplication
    class TestConfig

}
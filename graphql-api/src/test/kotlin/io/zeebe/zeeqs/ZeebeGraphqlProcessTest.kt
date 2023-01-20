package io.zeebe.zeeqs

import io.camunda.zeebe.model.bpmn.Bpmn
import io.zeebe.zeeqs.data.entity.Process
import io.zeebe.zeeqs.data.repository.ProcessRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.web.server.LocalServerPort
import java.time.Instant

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestConfiguration
class ZeebeGraphqlProcessTest(
        @LocalServerPort private val port: Int,
        @Autowired val processRepository: ProcessRepository) {

    private val graphqlAssertions = GraphqlAssertions(port = port)

    private val processDefinitionKey = 10L

    @BeforeEach
    fun `deploy process`() {
        val process = Bpmn.createExecutableProcess("process")
                .startEvent("start")
                .sequenceFlowId("to-task")
                .serviceTask("service-task")
                .zeebeJobType("test")
                .sequenceFlowId("to-end")
                .endEvent("end")
                .done();

        processRepository.save(
                Process(
                        key = processDefinitionKey,
                        bpmnProcessId = "process",
                        version = 1,
                        bpmnXML = Bpmn.convertToString(process),
                        deployTime = Instant.now().toEpochMilli(),
                        resourceName = "process.bpmn",
                        checksum = "checksum"
                )
        )
    }

    @Test
    fun `should query process`() {
        // when/then
        graphqlAssertions.assertQuery(
                query = """
                    {
                      processes {
                        nodes {
                          key
                          bpmnProcessId
                          version
                        }
                      }
                    }
                    """,
                expectedResponseBody = """
                    {
                      "data": {
                        "processes": {
                          "nodes": [
                            {
                              "key": "$processDefinitionKey",
                              "bpmnProcessId": "process",
                              "version": 1
                            }
                          ]
                        }
                      }
                    }                
                    """
        )
    }

    @Test
    fun `should get elements of process`() {
        // when/then
        graphqlAssertions.assertQuery(
                query = """
                    {
                      process(key: $processDefinitionKey) {
                        elements {
                          elementId
                        }
                      }
                    }
                    """,
                expectedResponseBody = """
                    {
                      "data": {
                        "process": {
                          "elements": [
                            {
                              "elementId": "to-task"
                            },
                            {
                              "elementId": "to-end"
                            },
                            {
                              "elementId": "end"
                            },
                            {
                              "elementId": "service-task"
                            },
                            {
                              "elementId": "start"
                            }
                          ]
                        }
                      }
                    }                
                    """
        )
    }

    @Test
    fun `should filter elements of process by their type`() {
        // when/then
        graphqlAssertions.assertQuery(
                query = """
                    {
                      process(key: $processDefinitionKey) {
                        elements(elementTypeIn: [SERVICE_TASK]) {
                          elementId
                        }
                      }
                    }
                    """,
                expectedResponseBody = """
                    {
                      "data": {
                        "process": {
                          "elements": [                            
                            {
                              "elementId": "service-task"
                            }
                          ]
                        }
                      }
                    }                
                    """
        )
    }

    @SpringBootApplication
    class TestConfiguration

}
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
import org.springframework.graphql.test.tester.GraphQlTester
import java.time.Instant


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestConfiguration
class ZeebeGraphqlProcessTest(
        @Autowired private val graphQlTester: GraphQlTester,
        @Autowired private val processRepository: ProcessRepository) {

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
        graphQlTester.document("""
                    {
                      processes {
                        nodes {
                          key
                          bpmnProcessId
                          version
                        }
                      }
                    }
                    """)
                .execute()
                .path("processes.nodes")
                .matchesJson("""
                    [
                        {
                          "key": "$processDefinitionKey",
                          "bpmnProcessId": "process",
                          "version": 1
                        }
                    ]              
                    """)
    }

    @Test
    fun `should get elements of process`() {
        // when/then
        graphQlTester.document("""
                    {
                      process(key: $processDefinitionKey) {
                        elements {
                          elementId
                        }
                      }
                    }
                    """)
                .execute()
                .path("process.elements")
                .matchesJson("""
                    [
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
                    """)
    }

    @Test
    fun `should filter elements of process by their type`() {
        // when/then
        graphQlTester.document("""
                    {
                      process(key: $processDefinitionKey) {
                        elements(elementTypeIn: [SERVICE_TASK]) {
                          elementId
                        }
                      }
                    }
                    """)
                .execute()
                .path("process.elements")
                .matchesJson("""
                    [                            
                        {
                          "elementId": "service-task"
                        }
                    ]           
                    """)
    }

    @SpringBootApplication
    class TestConfig

}
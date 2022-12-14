package io.zeebe.zeeqs

import io.camunda.zeebe.model.bpmn.Bpmn
import io.zeebe.zeeqs.data.entity.Process
import io.zeebe.zeeqs.data.entity.UserTask
import io.zeebe.zeeqs.data.repository.ProcessRepository
import io.zeebe.zeeqs.data.repository.UserTaskRepository
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

    @BeforeEach
    fun `deploy process`() {
        processRepository.save(
                Process(
                        key = 1,
                        bpmnProcessId = "process",
                        version = 1,
                        bpmnXML = "<...>",
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
                              "key": "1",
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

    @SpringBootApplication
    class TestConfiguration

}
package io.zeebe.zeeqs

import io.camunda.zeebe.model.bpmn.Bpmn
import io.zeebe.zeeqs.data.entity.Process
import io.zeebe.zeeqs.data.entity.ProcessInstance
import io.zeebe.zeeqs.data.entity.UserTask
import io.zeebe.zeeqs.data.repository.ProcessInstanceRepository
import io.zeebe.zeeqs.data.repository.ProcessRepository
import io.zeebe.zeeqs.data.repository.UserTaskRepository
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
class ZeebeGraphqlUserTaskTest(
        @Autowired private val graphQlTester: GraphQlTester,
        @Autowired val processRepository: ProcessRepository,
        @Autowired val processInstanceRepository: ProcessInstanceRepository,
        @Autowired val userTaskRepository: UserTaskRepository) {

    private val processDefinitionKey = 10L
    private val processInstanceKey = 20L
    private val processId = "process"
    private val userTaskKey = 30L
    private val formKey = "form1"

    @BeforeEach
    fun `create user task`() {
        val bpmn = Bpmn.createExecutableProcess(processId)
                .startEvent()
                .userTask("A")
                .zeebeUserTaskForm(formKey, """{"x":1}""")
                .endEvent()
                .done()

        processRepository.save(
                Process(
                        key = processDefinitionKey,
                        bpmnProcessId = processId,
                        version = 1,
                        bpmnXML = Bpmn.convertToString(bpmn),
                        deployTime = Instant.now().toEpochMilli(),
                        resourceName = "process.bpmn",
                        checksum = "checksum"
                )
        )

        processInstanceRepository.save(
                ProcessInstance(
                        key = processInstanceKey,
                        position = 1L,
                        bpmnProcessId = processId,
                        version = 1,
                        processDefinitionKey = processDefinitionKey,
                        parentProcessInstanceKey = null,
                        parentElementInstanceKey = null
                )
        )

        userTaskRepository.save(
                UserTask(
                        key = userTaskKey,
                        position = 1L,
                        processInstanceKey = processInstanceKey,
                        processDefinitionKey = processDefinitionKey,
                        elementInstanceKey = 1L,
                        assignee = "test",
                        candidateGroups = "[\"test-group\"]",
                        formKey = formKey
                )
        )
    }

    @Test
    fun `should query user task`() {
        // when/then
        graphQlTester.document("""
                    {
                      userTasks {
                        nodes {
                          key
                          assignee
                          candidateGroups
                          form {
                            key
                            resource
                          }
                        }
                      }
                    } 
                    """)
                .execute()
                .path("userTasks.nodes")
                .matchesJson("""
                    [
                        {
                          "key": "$userTaskKey",
                          "assignee": "test",
                          "candidateGroups": "[\"test-group\"]",
                          "form": {
                            "key": "$formKey",
                            "resource": "{\"x\":1}"
                          }
                        }
                    ]              
                    """)
    }

    @Test
    fun `should query user tasks of process instance`() {
        // when/then
        graphQlTester.document("""
                    {
                      processInstance(key: $processInstanceKey) {
                        userTasks {
                          nodes {
                            key
                          }
                        }
                      }
                    } 
                    """)
                .execute()
                .path("processInstance.userTasks.nodes")
                .matchesJson("""
                    [
                      {
                        "key": "$userTaskKey"
                      }
                    ]             
                    """)
    }

    @SpringBootApplication
    class TestConfig

}
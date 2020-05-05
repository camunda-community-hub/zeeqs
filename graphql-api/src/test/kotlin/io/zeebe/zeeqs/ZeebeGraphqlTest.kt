package io.zeebe.zeeqs

import io.zeebe.zeeqs.data.entity.Workflow
import io.zeebe.zeeqs.data.repository.WorkflowRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
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
class ZeebeGraphqlTest(
        @Autowired val environment: Environment,
        @Autowired val workflowRepository: WorkflowRepository) {

    val serverPort = environment.get("local.server.port")

    @Test
    fun `should query workflow`() {
        // given
        workflowRepository.save(Workflow(
                key = 1,
                bpmnProcessId = "wf",
                version = 1,
                bpmnXML = "<...>",
                deployTime = Instant.now().toEpochMilli()
        ));

        // when
        val response = sendQuery("{workflows{nodes{key,bpmnProcessId,version}}}")

        // then
        assertThat(response.statusCode()).isEqualTo(200)
        assertThat(response.body()).isEqualToIgnoringWhitespace("""
            {"data":
            {"workflows":
            {"nodes":[
            {"key":"1",
            "bpmnProcessId":"wf", 
            "version":1}
            ]}}}""".trimIndent())
    }

    private fun sendQuery(query: String): HttpResponse<String> {
        val encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8)
        val uri = "http://localhost:$serverPort/graphql?query=$encodedQuery"

        val request = HttpRequest.newBuilder()
                .uri(URI(uri))
                .GET()
                .build()

        return HttpClient
                .newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString())
    }

    @SpringBootApplication
    class TestConfiguration

}
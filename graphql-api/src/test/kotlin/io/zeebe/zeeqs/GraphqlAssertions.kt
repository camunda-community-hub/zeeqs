package io.zeebe.zeeqs

import org.assertj.core.api.Assertions
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class GraphqlAssertions(private val port: Int) {

    fun assertQuery(query: String, expectedResponseBody: String) {
        val response = sendQuery(query)

        Assertions.assertThat(response.statusCode())
                .describedAs(
                        "Expect the request to be successful but the status code was '%s'.",
                        response.statusCode(),
                        response.toString())
                .isEqualTo(200)
        Assertions.assertThat(response.body()).isEqualToIgnoringWhitespace(expectedResponseBody)
    }

    private fun sendQuery(query: String): HttpResponse<String> {
        val encodedQuery = query.replace("\n", "")
        val uri = "http://localhost:$port/graphql"

        val request = HttpRequest.newBuilder()
                .uri(URI(uri))
                .POST(HttpRequest.BodyPublishers.ofString("""{"query": "$encodedQuery"}"""))
                .header("Content-Type", "application/json")
                .build()

        return HttpClient
                .newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString())
    }

}
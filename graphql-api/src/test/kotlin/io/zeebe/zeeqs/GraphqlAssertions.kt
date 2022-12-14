package io.zeebe.zeeqs

import org.assertj.core.api.Assertions
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.stereotype.Component
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

class GraphqlAssertions(private val port: Int) {

    fun assertQuery(query: String, expectedResponseBody: String) {
        val response = sendQuery(query)

        Assertions.assertThat(response.statusCode()).isEqualTo(200)
        Assertions.assertThat(response.body()).isEqualToIgnoringWhitespace(expectedResponseBody)
    }

    private fun sendQuery(query: String): HttpResponse<String> {
        val encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8)
        val uri = "http://localhost:$port/graphql?query=$encodedQuery"

        val request = HttpRequest.newBuilder()
                .uri(URI(uri))
                .GET()
                .build()

        return HttpClient
                .newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString())
    }

}
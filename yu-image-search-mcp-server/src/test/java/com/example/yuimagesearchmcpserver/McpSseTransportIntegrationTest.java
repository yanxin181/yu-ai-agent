package com.example.yuimagesearchmcpserver;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "server.shutdown=immediate"
)
class McpSseTransportIntegrationTest {

    @LocalServerPort
    private int port;

    @Test
    void exposesMcpSseEndpointOverHttp() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/sse"))
                .header("Accept", "text/event-stream")
                .timeout(Duration.ofSeconds(3))
                .GET()
                .build();

        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpResponse<java.io.InputStream> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofInputStream()
            );

            assertEquals(200, response.statusCode());
            assertTrue(response.headers().firstValue("Content-Type")
                    .orElse("")
                    .startsWith("text/event-stream"));
            response.body().close();
        }
    }
}

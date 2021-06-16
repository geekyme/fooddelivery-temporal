package io.shawnlim.eats.courier.configuration;

import org.mockserver.integration.ClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class HttpConfiguration {
  private Integer mockServerPort = 1080;

  @Bean
  public ClientAndServer startServer() {
    ClientAndServer mock = ClientAndServer.startClientAndServer(mockServerPort);

    mock
    .when(
      request()
        .withMethod("GET")
        .withPath("/mock")
    ).respond(
      response()
        .withStatusCode(200)
        .withBody("mock response")
        .withDelay(TimeUnit.SECONDS, 2)
    );

    return mock;
  }

  @Bean
  WebClient httpClient() {
    return WebClient.create("http://localhost:" + mockServerPort.toString());
  }
}

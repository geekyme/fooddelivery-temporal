package io.shawnlim.eats.courier.activities;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.web.reactive.function.client.WebClient;

import io.temporal.failure.ApplicationFailure;


public class CourierOrderActivitiesImpl implements CourierOrderActivities {
  private WebClient httpClient;

  public CourierOrderActivitiesImpl(WebClient mockHttpClient) {
    this.httpClient = mockHttpClient;
  }

  public void sendOrder(String eta, long startTime) {
    long currentTime = System.currentTimeMillis();
    
    // TODO: hardcoded to 1 hr - if order is already 1 min late, then trigger a failure
    // this can be dynamically retrieved from DB instead;
    long INTERVAL = 60000 * 60; 
    
    if (currentTime - startTime > INTERVAL) {
      throw ApplicationFailure.newNonRetryableFailure("Time interval exceeded allowed time!", "ORDER_TIMEOUT");
    }

    System.out.println("Driver order sent with ETA: " + eta);
  }

  public void sendCustomerNotification(String message) {
    System.out.println("Sending notification ...");
    String result = httpClient.get().uri("/mock").retrieve().bodyToMono(String.class).block();
    System.out.println("Notification sent to customer: " + message);
    System.out.println("Result is :" + result);
  }

  public void recordCancellation() {
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm:ss");
    LocalDateTime now = LocalDateTime.now();
    System.out.println("Driver cancelled at: " + dtf.format(now));
  }

  public String deliverOrder() {
    System.out.println("order delivered");
    return "deliver done";
  }
}

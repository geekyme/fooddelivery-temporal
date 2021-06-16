package io.shawnlim.eats.issuance.activities;

import io.shawnlim.eats.issuance.generated.types.OrderInput;

public class IssuanceActivitiesImpl implements IssuanceActivities {
  public void processOrder(OrderInput order) {
    System.out.println(order);
  }

  public void trackCorpTech() {
    System.out.println("tracking.... corptech");
  }

  public void escalateOrder(OrderInput order) {
    System.out.println("Order escalated for investigation: " + order);
  }

  public void processRefund(OrderInput order) {
    System.out.println("refunding for order " + order);
  }
}

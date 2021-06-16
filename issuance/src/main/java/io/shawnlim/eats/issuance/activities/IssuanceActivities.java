package io.shawnlim.eats.issuance.activities;

import io.shawnlim.eats.issuance.generated.types.OrderInput;
import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface IssuanceActivities {
  void trackCorpTech();

  void processOrder(OrderInput order);

  void escalateOrder(OrderInput order);

  void processRefund(OrderInput order);
}

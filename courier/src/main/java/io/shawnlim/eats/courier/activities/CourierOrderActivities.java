package io.shawnlim.eats.courier.activities;

import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface CourierOrderActivities {
  public static final String COURIER_ORDER_ACTIVITIES_TASK_QUEUE = "CourierOrderActivitiesQueue";

  public void sendOrder(String eta, long startTime);

  public void recordCancellation();

  public void sendCustomerNotification(String message);

  public String deliverOrder();
}

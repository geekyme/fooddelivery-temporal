package io.shawnlim.eats.restaurant.activities;

import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface RestaurantOrderActivities {
  public static final String RESTAURANT_ORDER_ACTIVITIES_TASK_QUEUE = "RestaurantOrderActivitiesQueue";

  public void sendOrder();

  public void cancelOrder();

  public String estimateETA();
}

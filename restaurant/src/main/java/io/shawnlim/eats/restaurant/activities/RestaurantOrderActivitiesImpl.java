package io.shawnlim.eats.restaurant.activities;

public class RestaurantOrderActivitiesImpl implements RestaurantOrderActivities {
  public void sendOrder() {
    System.out.println("order sent");
  }

  public void cancelOrder() {
    System.out.println("order cancelled on restaurant");
  }

  public String estimateETA() {
    System.out.println("order received and time estimated");
    return "In 30 mins"; // TODO: proper time estimation
  }
}

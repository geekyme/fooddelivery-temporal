package io.shawnlim.eats.restaurant.workflows;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

import io.shawnlim.eats.restaurant.activities.RestaurantOrderActivities;
import io.shawnlim.eats.restaurant.api.RestaurantOrderWorkflow;
import io.temporal.activity.ActivityCancellationType;
import io.temporal.activity.ActivityOptions;
import io.temporal.failure.CanceledFailure;
import io.temporal.workflow.CancellationScope;
import io.temporal.workflow.Workflow;

public class RestaurantOrderWorkflowImpl implements RestaurantOrderWorkflow {
  private final RestaurantOrderActivities activities;
  private String status;

  public RestaurantOrderWorkflowImpl() {
    this.activities = Workflow.newActivityStub(RestaurantOrderActivities.class,
        ActivityOptions.newBuilder()
          .setTaskQueue(RestaurantOrderActivities.RESTAURANT_ORDER_ACTIVITIES_TASK_QUEUE)
          .setCancellationType(ActivityCancellationType.WAIT_CANCELLATION_COMPLETED)
          .setStartToCloseTimeout(Duration.ofHours(1)).build());
  }

  @Override
  public String handleOrder(Boolean isLoadtest) {
    try {
      activities.sendOrder();

      if (isLoadtest != null) {
        this.status = "ACCEPTED";
      }

      Workflow.await(() -> Objects.equals(status, "ACCEPTED"));
      String eta = activities.estimateETA();
      return eta;  
    } catch (CanceledFailure e) {
      // this needs to run in a detached scope as the main workflow is canceled
      CancellationScope detached = Workflow.newDetachedCancellationScope(() -> {
        activities.cancelOrder();
      });

      detached.run();

      return "order cancelled";
    }
    //TODO: handle other exception
  }

  @Override
  public void acceptOrder() {
    this.status = "ACCEPTED";
  }

  @Override
  public String getStatus() {
    return this.status;
  }
}

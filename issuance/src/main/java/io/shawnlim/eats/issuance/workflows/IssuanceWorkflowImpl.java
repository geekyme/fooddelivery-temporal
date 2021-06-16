package io.shawnlim.eats.issuance.workflows;

import java.time.Duration;
import java.util.Optional;

import io.shawnlim.eats.courier.api.CourierOrderWorkflow;
import io.shawnlim.eats.issuance.TemporalConfiguration;
import io.shawnlim.eats.issuance.activities.IssuanceActivities;
import io.shawnlim.eats.issuance.generated.types.OrderInput;
import io.shawnlim.eats.restaurant.api.RestaurantOrderWorkflow;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.failure.ApplicationFailure;
import io.temporal.failure.CanceledFailure;
import io.temporal.failure.ChildWorkflowFailure;
import io.temporal.workflow.CancellationScope;
import io.temporal.workflow.ChildWorkflowCancellationType;
import io.temporal.workflow.ChildWorkflowOptions;
import io.temporal.workflow.Workflow;

public class IssuanceWorkflowImpl implements IssuanceWorkflow {
  private final IssuanceActivities activities;

  public IssuanceWorkflowImpl() {
    this.activities = Workflow.newActivityStub(IssuanceActivities.class,
        ActivityOptions.newBuilder()
          .setTaskQueue(TemporalConfiguration.ISSUANCE_ACTIVITIES_TASK_QUEUE)
          .setStartToCloseTimeout(Duration.ofHours(1)).build());
  }

  @Override
  public String start(OrderInput order, Boolean isLoadtest) {
    RestaurantOrderWorkflow restaurantOrderWorkflow  = Workflow.newChildWorkflowStub(
      RestaurantOrderWorkflow.class,
      ChildWorkflowOptions.newBuilder()
        .setTaskQueue(RestaurantOrderWorkflow.RESTAURANT_ORDER_WORKFLOW_TASK_QUEUE)
        .setWorkflowId("restaurant-" + order.getId())
        .setCancellationType(ChildWorkflowCancellationType.WAIT_CANCELLATION_COMPLETED)
        .build());

    CourierOrderWorkflow courierOrderWorkflow = Workflow.newChildWorkflowStub(
      CourierOrderWorkflow.class,
      ChildWorkflowOptions.newBuilder()
        .setTaskQueue(CourierOrderWorkflow.COURIER_ORDER_WORKFLOW_TASK_QUEUE)
        .setWorkflowId("courier-" + order.getId())
        .setCancellationType(ChildWorkflowCancellationType.WAIT_CANCELLATION_COMPLETED)
        .build());

    long startTime = Workflow.currentTimeMillis();

    String eta;
    try {
      activities.processOrder(order);
      eta = restaurantOrderWorkflow.handleOrder(isLoadtest);
    } catch (ChildWorkflowFailure e) {
      if (e.getCause() instanceof CanceledFailure) {
        // this needs to run in a detached scope as the main workflow is canceled
        CancellationScope detached = Workflow.newDetachedCancellationScope(() -> {
          activities.processRefund(order);
        });
  
        detached.run();
  
        return "customer cancelled";
      }

      throw e;
    }

    try {
      // We use Workflow.retry manually instead of calling `.setRetryOptions(retryOptions)` on courierOrderWorkflow
      // because we want to retry on CanceledFailure
      String result = Workflow.retry(RetryOptions.newBuilder()
        .setDoNotRetry("ORDER_TIMEOUT")
        // we handle workflow retry logic using Application.newNonRetryableFailure
        .build(), Optional.empty(), () -> {
          return courierOrderWorkflow.handleOrder(eta, startTime, isLoadtest);
        });
      
      activities.trackCorpTech();

      return result;  
    } catch (ChildWorkflowFailure e) {
      if (e.getCause() instanceof CanceledFailure) {
        // this needs to run in a detached scope as the main workflow is canceled
        CancellationScope detached = Workflow.newDetachedCancellationScope(() -> {
          activities.escalateOrder(order);
        });
  
        detached.run();
  
        return "Can't find any drivers because of multiple cancellations";
      } else if (e.getCause() instanceof ApplicationFailure) {
        ApplicationFailure cause = (ApplicationFailure) e.getCause();

        if ("ORDER_TIMEOUT".equals(cause.getType())) {
            activities.processRefund(order);
            
            return "order timeout and refunded";
        }
      }
      
      //TODO: handle other checked exceptions but otherwise, you need to wrap
      throw Workflow.wrap(e);
    }
  }
}

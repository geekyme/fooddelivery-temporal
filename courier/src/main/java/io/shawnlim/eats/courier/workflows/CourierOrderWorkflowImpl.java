package io.shawnlim.eats.courier.workflows;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

import io.shawnlim.eats.courier.activities.CourierOrderActivities;
import io.shawnlim.eats.courier.api.CourierOrderWorkflow;
import io.temporal.activity.ActivityOptions;
import io.temporal.failure.ApplicationFailure;
import io.temporal.failure.CanceledFailure;
import io.temporal.workflow.CancellationScope;
import io.temporal.workflow.Workflow;

public class CourierOrderWorkflowImpl implements CourierOrderWorkflow {
  private final CourierOrderActivities activities = Workflow.newActivityStub(CourierOrderActivities.class,
    ActivityOptions.newBuilder()
      .setTaskQueue(CourierOrderActivities.COURIER_ORDER_ACTIVITIES_TASK_QUEUE)
      .setStartToCloseTimeout(Duration.ofHours(1)).build());

  private String status;
  private boolean driverNearCustomer = false;
  private boolean orderDelivered = false;

  @Override
  public String handleOrder(String eta, long startTime, Boolean isLoadtest) {
    try {
      activities.sendOrder(eta, startTime);
      
      if (isLoadtest != null) {
        this.status = "ACCEPTED";
        this.driverNearCustomer = true;
        this.orderDelivered = true;
      }
      
      Workflow.await(() -> Objects.equals(status, "ACCEPTED"));
      
      Workflow.await(() -> {
        return orderDelivered || driverNearCustomer;
      });
      

      /*
       * Driver gps is problematic and driver still 2km away
       * but in reality, driver already on customer site, and press complete order
       */ 
      if (driverNearCustomer) {
        activities.sendCustomerNotification("Driver is around the corner");
      }
      
      Workflow.await(() -> {
        return orderDelivered;
      });

      activities.sendCustomerNotification("Order is delivered, enjoy your food!");
      String result = activities.deliverOrder();
      return result;
    } catch (CanceledFailure e) {
      // this needs to run in a detached scope as the main workflow is canceled
      CancellationScope detached = Workflow.newDetachedCancellationScope(() -> {
        activities.recordCancellation();
      });

      detached.run();

      return "canceled";
    } catch (Exception e) {
      // If this workflow failed because of a non retryable activity failure, rethrow this so the master workflow will not retry
      if (e.getCause() instanceof ApplicationFailure) {
        ApplicationFailure applicationFailure = (ApplicationFailure) e.getCause();
        if (applicationFailure.isNonRetryable()) {
          throw ApplicationFailure.newNonRetryableFailure(applicationFailure.getMessage(), applicationFailure.getType());
        } 
      }
      
      //TODO: handle other checked exceptions but otherwise, you need to wrap
      throw Workflow.wrap(e);
    }
  }

  @Override
  public void courierAcceptOrder() {
    this.status = "ACCEPTED";
  }

  @Override
  public void courierNearCustomer() {
    this.driverNearCustomer = true;
  }

  @Override
  public void courierDeliveredOrder() {
    this.orderDelivered = true;
  }

  @Override
  public String getStatus() {
    return this.status;
  }
}

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

public class SimpleWorkflowImpl implements SimpleWorkflow {
  private final IssuanceActivities activities;

  public SimpleWorkflowImpl() {
    this.activities = Workflow.newActivityStub(IssuanceActivities.class,
        ActivityOptions.newBuilder()
          .setTaskQueue(TemporalConfiguration.ISSUANCE_ACTIVITIES_TASK_QUEUE)
          .setStartToCloseTimeout(Duration.ofHours(1)).build());
  }

  @Override
  public String start(OrderInput order, Boolean isLoadtest) {
    activities.processOrder(order);
    activities.trackCorpTech();

    return "done";
  }
}

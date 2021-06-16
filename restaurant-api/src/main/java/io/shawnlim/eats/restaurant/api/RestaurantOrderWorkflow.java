package io.shawnlim.eats.restaurant.api;

import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface RestaurantOrderWorkflow {
  public static final String RESTAURANT_ORDER_WORKFLOW_TASK_QUEUE = "RestaurantOrderWorkflowQueue";

  @WorkflowMethod
  String handleOrder(Boolean isLoadtest);

  @SignalMethod
  void acceptOrder();

  @QueryMethod
  String getStatus();
}

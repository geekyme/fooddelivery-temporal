package io.shawnlim.eats.courier.api;

import java.util.Optional;

import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface CourierOrderWorkflow {
  public static final String COURIER_ORDER_WORKFLOW_TASK_QUEUE = "CourierOrderWorkflowQueue";

  @WorkflowMethod
  String handleOrder(String eta, long startTime, Boolean isLoadtest);

  @SignalMethod
  void courierAcceptOrder();

  // this signal will be invoked by the geofencing code when driver is near customer
  @SignalMethod
  void courierNearCustomer();

  // this signal will be invoked when the customer accepted order and driver click a button on his app to mark as delivered
  // TODO: handle fraud so driver cannot deliver order until he is at the customer location
  @SignalMethod
  void courierDeliveredOrder();

  @QueryMethod
  String getStatus();
}

package io.shawnlim.eats.courier;

import java.util.Optional;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.InputArgument;

import org.springframework.beans.factory.annotation.Autowired;

import io.shawnlim.eats.courier.api.CourierOrderWorkflow;
import io.shawnlim.eats.courier.api.generated.types.OrderInput;
import io.shawnlim.eats.courier.api.generated.types.OrderWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowNotFoundException;

@DgsComponent
public class CourierMutation {
  @Autowired
  WorkflowClient workflowClient;

  @DgsMutation
  public OrderWorkflow courierAcceptOrder(@InputArgument("order") OrderInput order) {
    try {
      CourierOrderWorkflow workflow = workflowClient.newWorkflowStub(CourierOrderWorkflow.class, order.getId());
  
      workflow.courierAcceptOrder();
  
      String status = workflow.getStatus();
  
      OrderWorkflow orderWorkflow = new OrderWorkflow(order.getId(), status);
  
      return orderWorkflow;
    } catch (WorkflowNotFoundException e) {
      throw new IllegalArgumentException("Workflow does not exist. Maybe it is already completed?");
    }
  }

  @DgsMutation
  public OrderWorkflow courierCancelOrder(@InputArgument("order") OrderInput order) {
    workflowClient.newUntypedWorkflowStub(order.getId(), Optional.empty(), Optional.empty()).cancel();

    // TODO: get the cancelled state
    OrderWorkflow orderWorkflow = new OrderWorkflow(order.getId(), "CANCELLED");

    return orderWorkflow;
  }

  @DgsMutation
  public OrderWorkflow courierNearCustomer(@InputArgument("order") OrderInput order) {
    try {
      CourierOrderWorkflow workflow = workflowClient.newWorkflowStub(CourierOrderWorkflow.class, order.getId());

      workflow.courierNearCustomer();

      String status = workflow.getStatus();

      OrderWorkflow orderWorkflow = new OrderWorkflow(order.getId(), status);

      return orderWorkflow;  
    } catch (WorkflowNotFoundException e) {
      throw new IllegalArgumentException("Workflow does not exist. Maybe it is already completed?");
    }
  }

  @DgsMutation
  public OrderWorkflow courierDeliveredOrder(@InputArgument("order") OrderInput order) {
    try {
      CourierOrderWorkflow workflow = workflowClient.newWorkflowStub(CourierOrderWorkflow.class, order.getId());

      workflow.courierDeliveredOrder();

      String status = workflow.getStatus();

      OrderWorkflow orderWorkflow = new OrderWorkflow(order.getId(), status);

      return orderWorkflow;  
    } catch (WorkflowNotFoundException e) {
      throw new IllegalArgumentException("Workflow does not exist. Maybe it is already completed?");
    }
  }
}

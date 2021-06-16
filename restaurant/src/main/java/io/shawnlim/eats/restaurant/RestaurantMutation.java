package io.shawnlim.eats.restaurant;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.InputArgument;

import org.springframework.beans.factory.annotation.Autowired;

import io.shawnlim.eats.restaurant.api.RestaurantOrderWorkflow;
import io.shawnlim.eats.restaurant.api.generated.types.OrderInput;
import io.shawnlim.eats.restaurant.api.generated.types.OrderWorkflow;
import io.temporal.client.WorkflowClient;

@DgsComponent
public class RestaurantMutation {
  @Autowired
  WorkflowClient workflowClient;

  @DgsMutation
  public OrderWorkflow acceptOrder(@InputArgument("order") OrderInput order) {
    RestaurantOrderWorkflow workflow = workflowClient.newWorkflowStub(RestaurantOrderWorkflow.class, order.getId());

    workflow.acceptOrder();

    String status = workflow.getStatus();

    OrderWorkflow orderWorkflow = new OrderWorkflow(order.getId(), status);

    return orderWorkflow;
  }
}

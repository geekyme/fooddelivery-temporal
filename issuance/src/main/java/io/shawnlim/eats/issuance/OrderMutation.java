package io.shawnlim.eats.issuance;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.InputArgument;

import org.springframework.beans.factory.annotation.Autowired;

import io.shawnlim.eats.issuance.generated.types.OrderInput;
import io.shawnlim.eats.issuance.generated.types.OrderWorkflow;
import io.shawnlim.eats.issuance.workflows.IssuanceWorkflow;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import static java.util.Optional.empty;

@DgsComponent
public class OrderMutation {
  @Autowired
  WorkflowClient workflowClient;

  @DgsMutation
  public OrderWorkflow fulfillOrder(@InputArgument("order") OrderInput order, @InputArgument("isLoadtest") Boolean isLoadtest) {
    WorkflowOptions options = WorkflowOptions.newBuilder()
        .setTaskQueue(TemporalConfiguration.ISSUANCE_WORKFLOW_TASK_QUEUE)
        .setWorkflowId(order.getId()).build();
        
    IssuanceWorkflow workflow = workflowClient.newWorkflowStub(IssuanceWorkflow.class, options);

    WorkflowExecution workflowExecution = WorkflowClient.start(workflow::start, order, isLoadtest);

    OrderWorkflow orderWorkflow = new OrderWorkflow(workflowExecution.getWorkflowId());

    return orderWorkflow;
  }

  @DgsMutation
  public OrderWorkflow cancelOrder(@InputArgument("order") OrderInput order) {
    workflowClient.newUntypedWorkflowStub(order.getId(), empty(), empty()).cancel();

    OrderWorkflow orderWorkflow = new OrderWorkflow(order.getId());

    return orderWorkflow;
  }
}

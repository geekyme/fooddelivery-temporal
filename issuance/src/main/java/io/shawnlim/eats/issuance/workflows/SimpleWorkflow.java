package io.shawnlim.eats.issuance.workflows;

import java.util.Optional;

import io.shawnlim.eats.issuance.generated.types.OrderInput;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/*
 * Workflow is a native Temporal concept that represents a series of durable functions (Activities)
 * We model our workflow here as the typical entry point after an issuance component consumes message from an SQS
 */
@WorkflowInterface
public interface SimpleWorkflow {
  @WorkflowMethod
  String start(OrderInput order, Boolean isLoadtest);
}

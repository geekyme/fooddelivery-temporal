package io.shawnlim.eats.restaurant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.shawnlim.eats.restaurant.activities.RestaurantOrderActivities;
import io.shawnlim.eats.restaurant.activities.RestaurantOrderActivitiesImpl;
import io.shawnlim.eats.restaurant.api.RestaurantOrderWorkflow;
import io.shawnlim.eats.restaurant.workflows.RestaurantOrderWorkflowImpl;
import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.worker.WorkerFactoryOptions;
import io.temporal.worker.WorkerOptions;

@Configuration
public class TemporalConfiguration {
  @Value("${temporal.host}")
  String temporalHost;

  @Value("${temporal.worker-threads}")
  Integer workerThreads;

  @Value("${temporal.max-activities}")
  Integer maxActivities;

  @Value("${temporal.max-workflows}")
  Integer maxWorkflows;

  @Bean
  public WorkflowClient client() {
    WorkflowServiceStubsOptions options = WorkflowServiceStubsOptions.newBuilder().setTarget(temporalHost).build();
    WorkflowServiceStubs service = WorkflowServiceStubs.newInstance(options);
    WorkflowClient client = WorkflowClient.newInstance(service);
    
    initializeWorkers(client);

    return client;
  }

  private void initializeWorkers(WorkflowClient client) {
    WorkerFactory factory = WorkerFactory.newInstance(client, WorkerFactoryOptions.newBuilder()
      .setWorkflowHostLocalPollThreadCount(workerThreads)
      .build()
    );

    // its better to separate activity and workflow workers, 
    // and usually this is done in separate processes to scale up these things individually
    Worker workflowWorker = factory.newWorker(RestaurantOrderWorkflow.RESTAURANT_ORDER_WORKFLOW_TASK_QUEUE, WorkerOptions.newBuilder()
      .setWorkflowPollThreadCount(workerThreads / 2)
      .setMaxConcurrentWorkflowTaskExecutionSize(maxWorkflows)
      .build()
    );

    Worker activityWorker = factory.newWorker(RestaurantOrderActivities.RESTAURANT_ORDER_ACTIVITIES_TASK_QUEUE, WorkerOptions.newBuilder()
      .setActivityPollThreadCount(workerThreads / 2)
      .setMaxConcurrentActivityExecutionSize(maxActivities)
      .build()
    );
    
    workflowWorker.registerWorkflowImplementationTypes(RestaurantOrderWorkflowImpl.class);
    activityWorker.registerActivitiesImplementations(new RestaurantOrderActivitiesImpl());

    factory.start();
  }
}

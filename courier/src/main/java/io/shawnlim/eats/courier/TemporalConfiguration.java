package io.shawnlim.eats.courier;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import io.shawnlim.eats.courier.activities.CourierOrderActivities;
import io.shawnlim.eats.courier.activities.CourierOrderActivitiesImpl;
import io.shawnlim.eats.courier.api.CourierOrderWorkflow;
import io.shawnlim.eats.courier.workflows.CourierOrderWorkflowImpl;
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

  @Value("${temporal.activity-threads}")
  Integer activityThreads;

  @Bean
  public WorkflowClient client(WebClient mockHttpClient) {
    WorkflowServiceStubsOptions options = WorkflowServiceStubsOptions.newBuilder().setEnableHttps(true).setTarget(temporalHost).build();
    WorkflowServiceStubs service = WorkflowServiceStubs.newInstance(options);
    WorkflowClient client = WorkflowClient.newInstance(service);
    
    initializeWorkers(client, mockHttpClient);

    return client;
  }
  
  private void initializeWorkers(WorkflowClient client, WebClient mockHttpClient) {
    WorkerFactory factory = WorkerFactory.newInstance(client, WorkerFactoryOptions.newBuilder()
      .setWorkflowHostLocalPollThreadCount(workerThreads)
      .build()
    );

    // its better to separate activity and workflow workers, 
    // and usually this is done in separate processes to scale up these things individually
    Worker workflowWorker = factory.newWorker(CourierOrderWorkflow.COURIER_ORDER_WORKFLOW_TASK_QUEUE, WorkerOptions.newBuilder()
      .setActivityPollThreadCount(activityThreads)
      .build()
    );

    Worker activityWorker = factory.newWorker(CourierOrderActivities.COURIER_ORDER_ACTIVITIES_TASK_QUEUE, WorkerOptions.newBuilder()
      .setActivityPollThreadCount(activityThreads)
      .build()
    );
    
    workflowWorker.registerWorkflowImplementationTypes(CourierOrderWorkflowImpl.class);
    activityWorker.registerActivitiesImplementations(new CourierOrderActivitiesImpl(mockHttpClient));

    factory.start();
  }
}

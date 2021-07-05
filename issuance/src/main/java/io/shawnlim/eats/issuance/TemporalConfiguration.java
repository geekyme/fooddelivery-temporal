package io.shawnlim.eats.issuance;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.shawnlim.eats.issuance.activities.IssuanceActivitiesImpl;
import io.shawnlim.eats.issuance.workflows.IssuanceWorkflowImpl;
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

  public static final String ISSUANCE_WORKFLOW_TASK_QUEUE = "IssuanceWorkflowQueue";
  public static final String ISSUANCE_ACTIVITIES_TASK_QUEUE = "IssuanceActivitiesQueue";

  @Bean
  public WorkflowClient client() {
    WorkflowServiceStubsOptions options = WorkflowServiceStubsOptions.newBuilder().setEnableHttps(true).setTarget(temporalHost).build();
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
    Worker workflowWorker = factory.newWorker(ISSUANCE_WORKFLOW_TASK_QUEUE, WorkerOptions.newBuilder()
      .setWorkflowPollThreadCount(workerThreads / 2)
      .build()
    );
    Worker activityWorker = factory.newWorker(ISSUANCE_ACTIVITIES_TASK_QUEUE, WorkerOptions.newBuilder()
      .setActivityPollThreadCount(workerThreads / 2)
      .build()
    );
    
    workflowWorker.registerWorkflowImplementationTypes(IssuanceWorkflowImpl.class);
    activityWorker.registerActivitiesImplementations(new IssuanceActivitiesImpl());

    factory.start();
  }
}

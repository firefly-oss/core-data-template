package com.firefly.core.data.template.core.services;

import org.fireflyframework.data.model.JobStage;
import org.fireflyframework.data.model.JobStageRequest;
import org.fireflyframework.data.model.JobStageResponse;
import org.fireflyframework.data.observability.JobMetricsService;
import org.fireflyframework.data.observability.JobTracingService;
import org.fireflyframework.data.orchestration.model.JobExecutionRequest;
import org.fireflyframework.data.orchestration.port.JobOrchestrator;
import org.fireflyframework.data.resiliency.ResiliencyDecoratorService;
import org.fireflyframework.data.service.AbstractResilientDataJobService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service("orderDataJobService")
@Slf4j
public class OrderDataJobService extends AbstractResilientDataJobService {

    private final JobOrchestrator jobOrchestrator;

    public OrderDataJobService(
            JobTracingService tracingService,
            JobMetricsService metricsService,
            ResiliencyDecoratorService resiliencyService,
            JobOrchestrator jobOrchestrator) {
        super(tracingService, metricsService, resiliencyService);
        this.jobOrchestrator = jobOrchestrator;
    }

    @Override
    protected Mono<JobStageResponse> doStartJob(JobStageRequest request) {
        log.debug("Starting order data extraction");

        JobExecutionRequest executionRequest = JobExecutionRequest.builder()
                .jobDefinition("order-data-extraction")
                .input(request.getParameters())
                .build();

        return jobOrchestrator.startJob(executionRequest)
                .map(execution -> JobStageResponse.success(
                        JobStage.START,
                        execution.getExecutionId(),
                        "Order data extraction started"
                ));
    }

    @Override
    protected Mono<JobStageResponse> doCheckJob(JobStageRequest request) {
        return jobOrchestrator.checkJobStatus(request.getExecutionId())
                .map(status -> JobStageResponse.success(
                        JobStage.CHECK,
                        request.getExecutionId(),
                        "Status: " + status
                ));
    }

    @Override
    protected Mono<JobStageResponse> doCollectJobResults(JobStageRequest request) {
        return jobOrchestrator.getJobExecution(request.getExecutionId())
                .map(execution -> JobStageResponse.builder()
                        .stage(JobStage.COLLECT)
                        .executionId(execution.getExecutionId())
                        .status(execution.getStatus())
                        .data(execution.getOutput())
                        .success(true)
                        .message("Order data collected")
                        .build());
    }

    @Override
    protected Mono<JobStageResponse> doGetJobResult(JobStageRequest request) {
        return doCollectJobResults(request);
    }

    @Override
    protected Mono<JobStageResponse> doStopJob(JobStageRequest request, String reason) {
        return jobOrchestrator.stopJob(request.getExecutionId(), reason)
                .map(status -> JobStageResponse.success(
                        JobStage.STOP,
                        request.getExecutionId(),
                        "Job stopped: " + status
                ));
    }

    @Override
    protected String getOrchestratorType() {
        return jobOrchestrator.getOrchestratorType();
    }

    @Override
    protected String getJobDefinition() {
        return "order-data-extraction";
    }
}

package com.firefly.core.data.template.core.services;

import com.firefly.common.data.model.JobStage;
import com.firefly.common.data.model.JobStageRequest;
import com.firefly.common.data.model.JobStageResponse;
import com.firefly.common.data.observability.JobMetricsService;
import com.firefly.common.data.observability.JobTracingService;
import com.firefly.common.data.orchestration.model.JobExecutionRequest;
import com.firefly.common.data.orchestration.port.JobOrchestrator;
import com.firefly.common.data.resiliency.ResiliencyDecoratorService;
import com.firefly.common.data.service.AbstractResilientDataJobService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service("customerDataJobService")  // Named bean for multiple services
@Slf4j
public class CustomerDataJobService extends AbstractResilientDataJobService {

    private final JobOrchestrator jobOrchestrator;

    public CustomerDataJobService(
            JobTracingService tracingService,
            JobMetricsService metricsService,
            ResiliencyDecoratorService resiliencyService,
            JobOrchestrator jobOrchestrator) {
        super(tracingService, metricsService, resiliencyService);
        this.jobOrchestrator = jobOrchestrator;
    }

    @Override
    protected Mono<JobStageResponse> doStartJob(JobStageRequest request) {
        log.debug("Starting customer data extraction");

        JobExecutionRequest executionRequest = JobExecutionRequest.builder()
                .jobDefinition("customer-data-extraction")
                .input(request.getParameters())
                .build();

        return jobOrchestrator.startJob(executionRequest)
                .map(execution -> JobStageResponse.success(
                        JobStage.START,
                        execution.getExecutionId(),
                        "Customer data extraction started"
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
                        .message("Customer data collected")
                        .build());
    }

    @Override
    protected Mono<JobStageResponse> doGetJobResult(JobStageRequest request) {
        // In a real implementation, use MapStruct mappers here
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
        return "customer-data-extraction";
    }
}
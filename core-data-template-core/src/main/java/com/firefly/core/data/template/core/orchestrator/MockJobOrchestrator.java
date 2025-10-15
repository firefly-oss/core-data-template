package com.firefly.core.data.template.core.orchestrator;

import com.firefly.common.data.orchestration.model.JobExecution;
import com.firefly.common.data.orchestration.model.JobExecutionRequest;
import com.firefly.common.data.orchestration.model.JobExecutionStatus;
import com.firefly.common.data.orchestration.port.JobOrchestrator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConditionalOnProperty(
        prefix = "firefly.data.orchestration",
        name = "orchestrator-type",
        havingValue = "MOCK"
)
@Slf4j
public class MockJobOrchestrator implements JobOrchestrator {

    private final Map<String, JobExecution> executions = new ConcurrentHashMap<>();

    @Override
    public Mono<JobExecution> startJob(JobExecutionRequest request) {
        String executionId = "exec-" + UUID.randomUUID().toString();

        JobExecution execution = JobExecution.builder()
                .executionId(executionId)
                .jobDefinition(request.getJobDefinition())
                .status(JobExecutionStatus.RUNNING)
                .input(request.getInput())
                .startTime(Instant.now())
                .build();

        executions.put(executionId, execution);
        log.info("MOCK: Started job '{}' with executionId '{}'",
                request.getJobDefinition(), executionId);

        return Mono.just(execution);
    }

    @Override
    public Mono<JobExecutionStatus> checkJobStatus(String executionId) {
        JobExecution execution = executions.get(executionId);
        if (execution == null) {
            return Mono.error(new IllegalArgumentException(
                    "Execution not found: " + executionId));
        }

        log.info("MOCK: Checking status for '{}' - {}",
                executionId, execution.getStatus());
        return Mono.just(execution.getStatus());
    }

    @Override
    public Mono<JobExecution> getJobExecution(String executionId) {
        JobExecution execution = executions.get(executionId);
        if (execution == null) {
            return Mono.error(new IllegalArgumentException(
                    "Execution not found: " + executionId));
        }

        // Simulate completed job with mock output
        JobExecution completed = JobExecution.builder()
                .executionId(executionId)
                .jobDefinition(execution.getJobDefinition())
                .input(execution.getInput())
                .status(JobExecutionStatus.SUCCEEDED)
                .stopTime(Instant.now())
                .output(generateMockOutput(execution.getJobDefinition()))
                .build();

        executions.put(executionId, completed);
        log.info("MOCK: Retrieved execution '{}'", executionId);

        return Mono.just(completed);
    }

    @Override
    public Mono<JobExecutionStatus> stopJob(String executionId, String reason) {
        JobExecution execution = executions.get(executionId);
        if (execution == null) {
            return Mono.error(new IllegalArgumentException(
                    "Execution not found: " + executionId));
        }

        log.info("MOCK: Stopping job '{}' - reason: {}", executionId, reason);
        return Mono.just(JobExecutionStatus.ABORTED);
    }

    @Override
    public String getOrchestratorType() {
        return "MOCK";
    }

    private Map<String, Object> generateMockOutput(String jobDefinition) {
        // Generate mock data based on job type
        return Map.of(
                "jobDefinition", jobDefinition,
                "status", "completed",
                "timestamp", Instant.now().toString(),
                "mockData", "This is mock output for " + jobDefinition
        );
    }
}

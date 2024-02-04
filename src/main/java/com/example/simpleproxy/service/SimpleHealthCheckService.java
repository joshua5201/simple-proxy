package com.example.simpleproxy.service;

import com.example.simpleproxy.config.UpstreamConfig;
import com.example.simpleproxy.model.Upstream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;

@Log4j2
@Service
@RequiredArgsConstructor
public class SimpleHealthCheckService implements InitializingBean, HealthCheckService {
    private final UpstreamConfig upstreamConfig;
    private final TaskScheduler taskScheduler;
    private final RestTemplate restTemplate;
    private UpstreamHealth[] healths;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.healths = new UpstreamHealth[upstreamConfig.getUpstreams().size()];
        for (int i = 0; i < upstreamConfig.getUpstreams().size(); i++) {
            this.healths[i] = new UpstreamHealth();
            taskScheduler.scheduleAtFixedRate(generateHealthCheckThread(i), Duration.ofSeconds(10));
        }
    }

    private Runnable generateHealthCheckThread(final int idx) {
        return () -> healthCheck(idx);
    }

    private void healthCheck(final int idx) {
        Upstream upstream = upstreamConfig.getUpstreams().get(idx);
        UpstreamHealth health = healths[idx];
        URI uri;
        try {
            uri = upstream.getUri(upstream.getHealth(), null);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid health check endpoint URL", e);
        }

        boolean success = false;

        try {
            ResponseEntity<String> responseEntity = restTemplate.getForEntity(uri, String.class);
            success = true;
            log.info("health check succeeded to upstream#{} {}, response: {}.", idx, upstream, responseEntity.getStatusCode());
        } catch (RestClientResponseException ex) {
            log.info("health check failed to upstream#{} {}, response: {}.", idx, upstream, ex.getStatusCode());
        } catch (RestClientException ex) {
            log.info("health check failed to upstream#{} {}, without response.", idx, upstream);
        }

        if (health.isHealthy()) {
            if (success) {
                return;
            }
            health.errorCount++;
            if (health.errorCount > upstreamConfig.getHealthCheckFailureCount()) {
                health.setHealthy(false);
                health.errorCount = 0;
                health.successCount = 0;
                log.info("health status for upstream#{} {} changed from healthy to unhealthy", idx, upstream);
            }
        } else {
            if (!success) {
                return;
            }
            health.successCount++;
            if (health.successCount > upstreamConfig.getHealthCheckSuccessCount()) {
                health.setHealthy(true);
                health.errorCount = 0;
                health.successCount = 0;
                log.info("health status for upstream#{} {} changed from unhealthy to healthy", idx, upstream);
            }
        }
    }

    @Override
    public boolean isHealthy(int upstreamIdx) {
        return healths[upstreamIdx].isHealthy();
    }

    @Getter
    @Setter
    private static class UpstreamHealth {
        private int errorCount;
        private int successCount;
        private boolean healthy = true;
    }
}

package com.example.simpleproxy.service;

import com.example.simpleproxy.config.UpstreamConfig;
import com.example.simpleproxy.error.NoAvailableHealthyUpstreamException;
import com.example.simpleproxy.model.Upstream;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
public class RoundRobinUpstreamService implements UpstreamService {
    private final UpstreamConfig upstreamConfig;
    private int nextIdx = 0;
    private final HealthCheckService healthCheckService;
    private final Lock upstreamListLock = new ReentrantLock();

    @Override
    @Nonnull
    public Upstream getNext() {
        boolean lockAcquired = false;
        try {
            lockAcquired = upstreamListLock.tryLock(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (!lockAcquired) {
            throw new NoAvailableHealthyUpstreamException();
        }

        Upstream upstream = null;
        final List<Upstream> upstreams = upstreamConfig.getUpstreams();
        final int size = upstreams.size();
        try {
            for (int i = 0; i < size; i++) {
                int j = (nextIdx + i) % size;
                if (healthCheckService.isHealthy(j)) {
                    upstream = upstreams.get(j);
                    nextIdx = (j + 1) % size;
                    break;
                }
            }
        } finally {
            upstreamListLock.unlock();
        }
        if (upstream == null) {
            throw new NoAvailableHealthyUpstreamException();
        }
        return upstream;
    }
}

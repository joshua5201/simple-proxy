package com.example.simpleproxy.service;

import com.example.simpleproxy.config.UpstreamConfig;
import com.example.simpleproxy.model.Upstream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class RoundRobinUpstreamService implements UpstreamService {
    /* TODO: use Queue to achieve round-robin thread-safely
     * poll with timeout: GATEWAY TIMEOUT if no available healthy upstreams
     * skip if node unhealthy (to achieve round-robin)
     */

    private final UpstreamConfig upstreamConfig;
    private final AtomicInteger nextUpstreamIdx = new AtomicInteger(0);

    @Override
    public Upstream getNext() {
        final List<Upstream> upstreams = upstreamConfig.getUpstreams();
        return upstreams.get(nextUpstreamIdx.getAndUpdate((i) -> {
            if (upstreams.size() - 1 == i) {
                return 0;
            }
            return i + 1;
        }));
    }
}

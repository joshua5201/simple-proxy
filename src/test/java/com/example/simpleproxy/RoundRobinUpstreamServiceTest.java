package com.example.simpleproxy;

import com.example.simpleproxy.config.UpstreamConfig;
import com.example.simpleproxy.model.Upstream;
import com.example.simpleproxy.service.RoundRobinUpstreamService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RoundRobinUpstreamServiceTest {
    @Mock
    private UpstreamConfig upstreamConfig;
    @InjectMocks
    private RoundRobinUpstreamService target;

    @Test
    void whenGetNextCalled_UpstreamReturnedInRoundRobin() {
        List<Upstream> upstreams = List.of(new Upstream("http", "127.0.0.1", 8080),
                new Upstream("https", "www.google.com", 1234),
            new Upstream("https", "www.google.com", 5566));
        when(upstreamConfig.getUpstreams()).thenReturn(upstreams);
        List<Upstream> upstreamsGot = new ArrayList<>();
        upstreamsGot.add(target.getNext());
        upstreamsGot.add(target.getNext());
        upstreamsGot.add(target.getNext());
        upstreamsGot.add(target.getNext());
        upstreamsGot.add(target.getNext());
        upstreamsGot.add(target.getNext());
        assertEquals(upstreams.get(0), upstreamsGot.get(0));
        assertEquals(upstreams.get(1), upstreamsGot.get(1));
        assertEquals(upstreams.get(2), upstreamsGot.get(2));
        assertEquals(upstreams.get(0), upstreamsGot.get(3));
        assertEquals(upstreams.get(1), upstreamsGot.get(4));
        assertEquals(upstreams.get(2), upstreamsGot.get(5));
        verify(upstreamConfig, times(6)).getUpstreams();
    }

    @Test
    void whenGetNextCalled_UpstreamReturnedInRoundRobinBetweenThreads() throws InterruptedException {
        List<Upstream> upstreams = List.of(new Upstream("http", "host1", 8080),
                new Upstream("http", "host2", 1234),
                new Upstream("http", "host3", 5566));
        when(upstreamConfig.getUpstreams()).thenReturn(upstreams);
        List<Upstream> upstreamsGot = new ArrayList<>();
        upstreamsGot.add(target.getNext());
        Thread anotherThread = new Thread(() -> {
            upstreamsGot.add(target.getNext());
            upstreamsGot.add(target.getNext());
            upstreamsGot.add(target.getNext());
        });
        anotherThread.start();
        anotherThread.join();
        upstreamsGot.add(target.getNext());
        assertEquals(upstreams.get(0), upstreamsGot.get(0));
        assertEquals(upstreams.get(1), upstreamsGot.get(1));
        assertEquals(upstreams.get(2), upstreamsGot.get(2));
        assertEquals(upstreams.get(0), upstreamsGot.get(3));
        assertEquals(upstreams.get(1), upstreamsGot.get(4));
        verify(upstreamConfig, times(5)).getUpstreams();
    }
}

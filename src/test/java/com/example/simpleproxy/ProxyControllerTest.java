package com.example.simpleproxy;

import com.example.simpleproxy.model.Upstream;
import com.example.simpleproxy.service.UpstreamService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProxyControllerTest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @MockBean
    private RestTemplate requestTestTemplate;

    @MockBean
    private UpstreamService upstreamService;

    @Captor
    private ArgumentCaptor<URI> uriArgumentCaptor;

    @Captor
    private ArgumentCaptor<HttpEntity<String>> httpEntityCaptor;

    private HttpEntity<String> getRequest(final String rawJson) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(rawJson, headers);
    }

    @Test
    void givenJsonRequest_SendSameRequestToUpstream_Return200Response() {
        Upstream upstream = new Upstream("http", "localhost", 8081);
        when(upstreamService.getNext()).thenReturn(upstream);
        final String rawJson = "{\"key\": \"val\"}";
        when(requestTestTemplate.postForEntity(any(URI.class), any(HttpEntity.class), eq((String.class)))).thenReturn(ResponseEntity.ok().body(rawJson));

        HttpEntity<String> requestEntity = getRequest(rawJson);

        ResponseEntity<String> responseEntity = testRestTemplate.postForEntity("http://localhost:" + port + "/abcde?qq=1", requestEntity, String.class);
        assertEquals(200, responseEntity.getStatusCode().value());

        verify(requestTestTemplate).postForEntity(uriArgumentCaptor.capture(), httpEntityCaptor.capture(), eq(String.class));
        URI requestUri = uriArgumentCaptor.getValue();

        assertEquals(upstream.getProtocol(), requestUri.getScheme());
        assertEquals(upstream.getHost(), requestUri.getHost());
        assertEquals(upstream.getPort(), requestUri.getPort());
        assertEquals("/abcde", requestUri.getPath());
        assertEquals("qq=1", requestUri.getQuery());

        HttpEntity<String> requestEntityToUpstream = httpEntityCaptor.getValue();
        assertEquals(requestEntity.getBody(), requestEntityToUpstream.getBody());
    }

    @Test
    void givenJsonRequest_SendSameRequestToUpstream_ReturnErrorResponseFromUpstream() {
        Upstream upstream = new Upstream("http", "localhost", 8081);
        when(upstreamService.getNext()).thenReturn(upstream);
        final String rawJson = "{\"key\": \"val\"}";
        when(requestTestTemplate.postForEntity(any(URI.class), any(HttpEntity.class), eq((String.class)))).thenReturn(ResponseEntity.badRequest().body(rawJson));

        HttpEntity<String> requestEntity = getRequest(rawJson);

        ResponseEntity<String> responseEntity = testRestTemplate.postForEntity("http://localhost:" + port + "/abcde?qq=1", requestEntity, String.class);
        assertEquals(400, responseEntity.getStatusCode().value());
    }
}

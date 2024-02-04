package com.example.simpleproxy.controller;

import com.example.simpleproxy.service.UpstreamService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;

@Log4j2
@Controller
@RequiredArgsConstructor
public class ProxyController {
    private final UpstreamService upstreamService;
    private final RestTemplate restTemplate;

    @PostMapping(value = "/**", consumes = MimeTypeUtils.APPLICATION_JSON_VALUE)
    private ResponseEntity<String> proxy(HttpEntity<String> request, HttpServletRequest httpServletRequest) {
        var upstream = upstreamService.getNext();
        URI uri;
        try {
            uri = new URI(upstream.getProtocol(), null, upstream.getHost(), upstream.getPort(),
                    httpServletRequest.getRequestURI(), httpServletRequest.getQueryString(), null);
        } catch (URISyntaxException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        log.info("Sending request with URL {} and body {}", uri, request.getBody());

        // handle errors
        try {
            return restTemplate.postForEntity(uri, request, String.class);
        } catch (ResourceAccessException ex) {
            log.info("Exception during request", ex);
            if (ex.getRootCause() != null && ex.getRootCause().getClass() == SocketTimeoutException.class) {
                return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).build();
            }
        } catch (RestClientResponseException ex) {
            log.info("Exception during request", ex);
            HttpStatusCode errorStatus = ex.getStatusCode();
            if (errorStatus.is4xxClientError()) {
                return ResponseEntity.status(errorStatus).body(ex.getResponseBodyAsString());
            }
        } catch (RestClientException ex) {
            log.info("Exception during request", ex);
        }
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
    }
}

package com.example.simpleproxy.model;

import lombok.*;

import java.net.URI;
import java.net.URISyntaxException;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Upstream {
    private String protocol;
    private String host;
    private int port = 80;
    private String health;

    public URI getUri(String path, String query) throws URISyntaxException {
        return new URI(getProtocol(), null, getHost(), getPort(),
                path, query, null);
    }
}

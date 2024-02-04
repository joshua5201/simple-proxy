package com.example.simpleproxy.config;

import com.example.simpleproxy.model.Upstream;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Validated
@ConfigurationProperties("simple-proxy")
public class UpstreamConfig {
    @NotEmpty
    private List<Upstream> upstreams = new ArrayList<>();
    private int backendConnectTimeout = 1000;
    private int backendReadTimeout = 1000;
}

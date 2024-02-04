package com.example.simpleproxy;

import com.example.simpleproxy.config.UpstreamConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SimpleProxyApplication {
	public static void main(String[] args) {
		SpringApplication.run(SimpleProxyApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate(UpstreamConfig upstreamConfig) {
		return new RestTemplateBuilder()
				.setConnectTimeout(Duration.ofMillis(upstreamConfig.getBackendConnectTimeout()))
				.setReadTimeout(Duration.ofMillis(upstreamConfig.getBackendReadTimeout()))
				.build();
	}
}

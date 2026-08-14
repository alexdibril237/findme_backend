package com.geolink.findme.admin.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient authServiceRestClient(@Value("${clients.auth-service.base-url}") String baseUrl,
                                             @Value("${clients.auth-service.timeout-ms:3000}") long timeoutMs) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory(timeoutMs))
                .build();
    }

    @Bean
    public RestClient addressServiceRestClient(@Value("${clients.address-service.base-url}") String baseUrl,
                                                @Value("${clients.address-service.timeout-ms:3000}") long timeoutMs) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory(timeoutMs))
                .build();
    }

    private ClientHttpRequestFactory requestFactory(long timeoutMs) {
        HttpClientSettings settings = HttpClientSettings.defaults()
                .withConnectTimeout(Duration.ofMillis(timeoutMs))
                .withReadTimeout(Duration.ofMillis(timeoutMs));
        return ClientHttpRequestFactoryBuilder.detect().build(settings);
    }
}

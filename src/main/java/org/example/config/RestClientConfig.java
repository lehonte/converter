package org.example.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Value("${convector.nbrb.url}")
    private String apiUrl;

    @Bean
    public RestClient getNbrbRate() {
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.defaults()
                .withConnectTimeout(Duration.ofSeconds(5))
                .withReadTimeout(Duration.ofSeconds(5));


        ClientHttpRequestFactory factory = ClientHttpRequestFactoryBuilder.detect().build(settings);

        return RestClient.builder()
                .requestFactory(factory)
                .baseUrl(apiUrl)
                .build();
    }
}

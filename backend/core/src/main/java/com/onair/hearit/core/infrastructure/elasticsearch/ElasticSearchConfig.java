package com.onair.hearit.core.infrastructure.elasticsearch;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.retry.annotation.Retryable;

@Retryable
@Configuration
@EnableElasticsearchRepositories(basePackages = "com.onair.hearit.core.infrastructure.elasticsearch.repository")
public class ElasticSearchConfig extends ElasticsearchConfiguration {

    private final String uris;
    private final String username;
    private final String password;

    public ElasticSearchConfig(@Value("${spring.elasticsearch.uris}") String uris,
                               @Value("${spring.elasticsearch.username}") String username,
                               @Value("${spring.elasticsearch.password}") String password) {
        this.uris = uris;
        this.username = username;
        this.password = password;
    }

    @Override
    public ClientConfiguration clientConfiguration() {
        return ClientConfiguration.builder()
                .connectedTo(uris)
                .withBasicAuth(username, password)
                .build();
    }
}

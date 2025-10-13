package com.onair.hearit.core.config;

import com.onair.hearit.core.infrastructure.datasource.DataSourceType;
import com.onair.hearit.core.infrastructure.datasource.RoutingDataSource;
import com.zaxxer.hikari.HikariDataSource;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

@Configuration
//@Profile("!integration-test")
public class DataSourceConfig {

    public static final String MASTER_PROPERTY = "spring.datasource.master";
    public static final String REPLICA_PROPERTY = "spring.datasource.replica";

    @Bean
    @ConfigurationProperties(MASTER_PROPERTY)
    public DataSource masterDataSource() {
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    @ConfigurationProperties(REPLICA_PROPERTY)
    public DataSource replicaDataSource() {
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    @DependsOn({"masterDataSource", "replicaDataSource"})
    public DataSource routingDataSource(
            @Qualifier("masterDataSource") DataSource master,
            @Qualifier("replicaDataSource") DataSource slave) {
        Map<Object, Object> dataSources = new HashMap<>();
        dataSources.put(DataSourceType.MASTER, master);
        dataSources.put(DataSourceType.REPLICA, slave);

        RoutingDataSource routingDataSource = new RoutingDataSource();
        routingDataSource.setDefaultTargetDataSource(master);
        routingDataSource.setTargetDataSources(dataSources);
        routingDataSource.afterPropertiesSet();
        return routingDataSource;
    }

    @Bean
    @Primary
    @DependsOn("routingDataSource")
    public DataSource dataSource(@Qualifier("routingDataSource") DataSource routingDataSource) {
        return new LazyConnectionDataSourceProxy(routingDataSource);
    }
}

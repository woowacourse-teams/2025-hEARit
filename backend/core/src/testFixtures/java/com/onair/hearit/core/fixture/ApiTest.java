package com.onair.hearit.core.fixture;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.web.server.LocalServerPort;

public abstract class ApiTest {

    @LocalServerPort
    protected int port;

    protected RequestSpecification spec;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        this.spec = new RequestSpecBuilder()
                .addHeader("Device-Uuid", UUID.randomUUID().toString())
                .build();
    }
}

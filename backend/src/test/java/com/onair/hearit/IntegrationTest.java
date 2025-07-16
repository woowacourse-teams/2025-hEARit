package com.onair.hearit;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration-test")
@Sql("/dbclean.sql")
public abstract class IntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    DBHelper dbHelper;

    @BeforeEach
    void setupPort() {
        RestAssured.port = port;
    }
}

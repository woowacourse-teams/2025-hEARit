package com.onair.hearit.presentation;

import com.onair.hearit.DbHelper;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@Sql("/dbclean.sql")
@ActiveProfiles("integration-test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class IntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    DbHelper dbHelper;

    @BeforeEach
    void setupPort() {
        RestAssured.port = port;
    }
}

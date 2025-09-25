package com.onair.hearit.fixture;

import com.onair.hearit.AdminTestApplication;
import com.onair.hearit.core.fixture.ApiTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@Sql("/dbclean.sql")
@ActiveProfiles("integration-test")
@SpringBootTest(
        classes = AdminTestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public abstract class IntegrationTest extends ApiTest {

    @Autowired
    protected DbHelper dbHelper;
}

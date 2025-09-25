package com.onair.hearit.admin.fixture;

import com.onair.hearit.AdminApplication;
import com.onair.hearit.core.fixture.ApiTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@Sql("/dbclean.sql")
@ActiveProfiles("integration-test")
@SpringBootTest(
        classes = AdminApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public abstract class IntegrationTest extends ApiTest {

    @Autowired
    protected DbHelper dbHelper;
}

package com.onair.hearit.app.fixture;

import com.onair.hearit.AppTestApplication;
import com.onair.hearit.app.exception.DomainExceptionMapper;
import com.onair.hearit.core.fixture.ApiTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@Sql("/dbclean.sql")
@ActiveProfiles("integration-test")
@SpringBootTest(
        classes = AppTestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Import(DomainExceptionMapper.class)
public abstract class IntegrationTest extends ApiTest {

    @Autowired
    protected DbHelper dbHelper;
}

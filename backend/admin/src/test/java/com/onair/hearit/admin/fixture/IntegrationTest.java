package com.onair.hearit.admin.fixture;

import com.onair.hearit.AdminTestApplication;
import com.onair.hearit.core.fixture.ApiTest;
import com.onair.hearit.core.infrastructure.elasticsearch.repository.HearitElasticSearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;

@Sql("/dbclean.sql")
@ActiveProfiles("integration-test")
@SpringBootTest(
        classes = AdminTestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public abstract class IntegrationTest extends ApiTest {

    @MockitoBean
    protected HearitElasticSearchRepository hearitElasticSearchRepository;

    @Autowired
    protected DbHelper dbHelper;
}

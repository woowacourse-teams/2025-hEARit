package com.onair.hearit.infrastructure;

import com.onair.hearit.config.TestJpaAuditingConfig;
import com.onair.hearit.fixture.DbHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import({ExploreScoreCommandRepository.class, DbHelper.class, TestJpaAuditingConfig.class})
@ActiveProfiles("integration-test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class ExploreScoreCommandRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DbHelper dbHelper;

    @Autowired
    private ExploreScoreCommandRepository exploreScoreCommandRepository;


}

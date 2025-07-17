package com.onair.hearit.application;

import java.util.Arrays;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("integration-test")
class MyTest {

    @Autowired
    DataSource dataSource;

    @Autowired
    Environment environment;

    @Test
    void checkDbConnectionInfo() throws Exception {
        String dbUrl = dataSource.getConnection().getMetaData().getURL();
        String[] activeProfiles = environment.getActiveProfiles();

        System.out.println("✅ Active Profiles = " + Arrays.toString(activeProfiles));
        System.out.println("✅ Loaded DB URL   = " + dbUrl);
    }
}

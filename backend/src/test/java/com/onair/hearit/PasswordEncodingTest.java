package com.onair.hearit;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncodingTest {

    @Test
    void encodePassword() {
        String rawPassword = "1234";
        String hashed = new BCryptPasswordEncoder().encode(rawPassword);
        System.out.println("hashed password = " + hashed);
    }
}

package com.example.refactored_student_housing.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards the seeding of week 1: users are seeded from data.sql (there is no
 * Java seeding routine), their passwords are stored as BCrypt hashes, and those
 * hashes really correspond to the credentials documented in README.md and on
 * the login page. A typo in a hash would otherwise only show up as a failed
 * login at demo time.
 */
class SeedDataTest {

    private static final Pattern BCRYPT_HASH = Pattern.compile("'(\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53})'");

    /** In the same order as the INSERT INTO app_users statement in data.sql. */
    private static final List<String> DOCUMENTED_PASSWORDS = List.of("admin123", "advisor123", "advisor123");

    @Test
    void seededPasswordsAreHashedAndMatchTheDocumentedCredentials() throws IOException {
        String sql = readDataSql();
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        List<String> hashes = new ArrayList<>();
        Matcher matcher = BCRYPT_HASH.matcher(sql);
        while (matcher.find()) {
            hashes.add(matcher.group(1));
        }

        assertEquals(DOCUMENTED_PASSWORDS.size(), hashes.size(),
                "data.sql should seed exactly the documented accounts");

        for (int i = 0; i < hashes.size(); i++) {
            assertTrue(encoder.matches(DOCUMENTED_PASSWORDS.get(i), hashes.get(i)),
                    "Seeded hash #" + (i + 1) + " does not match the documented password");
        }
    }

    @Test
    void noPlainTextPasswordIsSeeded() throws IOException {
        String sql = readDataSql();

        for (String password : DOCUMENTED_PASSWORDS) {
            assertTrue(!sql.contains("'" + password + "'"),
                    "data.sql must never contain a plain-text password");
        }
    }

    private String readDataSql() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/data.sql")) {
            assertNotNull(in, "data.sql should be on the classpath");
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}

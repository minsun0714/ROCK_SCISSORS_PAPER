package com.rsp.battle.user.presentation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ProfileImageUrlResolverTest {

    private ProfileImageUrlResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new ProfileImageUrlResolver();
        ReflectionTestUtils.setField(resolver, "baseUrl", "https://cdn.example.com");
    }

    @Test
    void resolveReturnsNullWhenKeyIsNull() {
        assertNull(resolver.resolve(null));
    }

    @Test
    void resolveConcatenatesBaseUrlAndKey() {
        assertEquals(
                "https://cdn.example.com/profile/a.png",
                resolver.resolve("profile/a.png")
        );
    }
}

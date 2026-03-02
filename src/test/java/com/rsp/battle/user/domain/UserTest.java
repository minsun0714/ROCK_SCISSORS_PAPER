package com.rsp.battle.user.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void createFromOAuth() {
        User user = User.createFromOAuth("user@example.com", "tester", "google");

        assertNull(user.getId());
        assertEquals("user@example.com", user.getEmail());
        assertEquals("tester", user.getNickname());
        assertEquals("google", user.getOauthProvider());
        assertNull(user.getProfileImageUrl());
        assertNull(user.getStatusMessage());
    }

    @Test
    void updateStatusMessage() {
        User user = User.createFromOAuth("user@example.com", "tester", "google");

        user.updateStatusMessage("hello");

        assertEquals("hello", user.getStatusMessage());
    }
}

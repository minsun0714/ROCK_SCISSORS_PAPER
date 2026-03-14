package com.rsp.battle.battle.domain;

import java.util.Arrays;

public enum Move {
    ROCK(0),
    PAPER(1),
    SCISSORS(2);

    private final int value;

    Move(int value) {
        this.value = value;
    }

    public static boolean isValid(String value) {
        return Arrays.stream(values())
                .anyMatch(v -> v.name().equals(value));
    }

    public int fight(Move opponent) {
        return (this.value - opponent.value + 3) % 3;
    }
}

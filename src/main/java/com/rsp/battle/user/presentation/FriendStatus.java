package com.rsp.battle.user.presentation;

/**
 * DB 테이블에서 사용하는 상태와 분리.
 * FreiendRequest에는 3가지 상태 (PENDING, ACCEPTED, REJECTED)가 있음
 */
public enum FriendStatus {
    NONE,
    REQUESTED,
    PENDING,
    FRIEND;
}

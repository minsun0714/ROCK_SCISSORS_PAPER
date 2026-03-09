package com.rsp.battle.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // ===== Common =====
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON_400", "잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_401", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "AUTH_403", "접근 권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_404", "리소스를 찾을 수 없습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500", "서버 오류가 발생했습니다."),

    // ===== User =====
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_404", "사용자를 찾을 수 없습니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "USER_409", "이미 사용 중인 닉네임입니다."),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "USER_400", "허용되지 않은 파일 타입입니다."),

    // ===== FriendRequest =====
    SELF_FRIEND_REQUEST_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "FRIEND_REQUEST_400", "자기 자신에게 친구 요청을 할 수 없습니다."),
    FRIEND_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "FRIEND_REQUEST_404", "친구 요청이 존재하지 않습니다."),
    DUPLICATE_FRIEND_REQUEST(HttpStatus.CONFLICT, "FRIEND_REQUEST_409_1", "이미 전송된 친구 요청입니다."),
    FRIEND_REQUEST_CLOSED(HttpStatus.CONFLICT, "FRIEND_REQUEST_409_2", "더 이상 존재하지 않는 친구 요청입니다."),

    // ===== BattleRequest =====
    SELF_BATTLE_REQUEST_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "BATTLE_REQUEST_400", "자기 자신에게 배틀 요청을 할 수 없습니다."),
    BATTLE_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "BATTLE_REQUEST_404", "배틀 요청이 존재하지 않습니다."),
    DUPLICATE_BATTLE_REQUEST(HttpStatus.CONFLICT, "BATTLE_REQUEST_409_1", "이미 전송된 배틀 요청입니다."),
    BATTLE_ROOM_CLOSED(HttpStatus.CONFLICT, "BATTLE_REQUEST_409_2", "더 이상 존재하지 않는 배틀 요청입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;


    ErrorCode(HttpStatus httpStatus, String code, String message) {
        this.status = httpStatus;
        this.code = code;
        this.message = message;
    }
}
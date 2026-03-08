package com.rsp.battle.user.persistence;

import com.rsp.battle.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("""
        SELECT u
        FROM User u
        WHERE u.email = :email
          AND u.oauthProvider = :provider
          AND u.deletedAt IS NULL
    """)
    Optional<User> findActiveUserByEmailAndAuthProvider(
            @Param("email") String email,
            @Param("provider") String provider
    );

    @Query(
            value = "SELECT * FROM users WHERE (LENGTH(:keyword) < 2 OR MATCH(nickname) AGAINST(:keyword IN BOOLEAN MODE)) AND deleted_at IS NULL",
            countQuery = "SELECT COUNT(*) FROM users WHERE (LENGTH(:keyword) < 2 OR MATCH(nickname) AGAINST(:keyword IN BOOLEAN MODE)) AND deleted_at IS NULL",
            nativeQuery = true
    )
    Page<User> searchByNickname(@Param("keyword") String keyword, Pageable pageable);

    @Query(
            value = """
                    SELECT u.* FROM users AS u JOIN friend_request AS fr
                    ON (u.id = fr.requester_id OR u.id = fr.receiver_id) AND u.id != :userId
                    WHERE
                    (fr.requester_id = :userId OR fr.receiver_id = :userId)
                    AND fr.status = 'ACCEPTED'
                    AND fr.active_flag IS NOT NULL
                    AND (LENGTH(:keyword) < 2 OR MATCH(u.nickname) AGAINST(:keyword IN BOOLEAN MODE))
                    AND u.deleted_at IS NULL
                    """,
            countQuery = """
                    SELECT COUNT(*) FROM users AS u JOIN friend_request AS fr
                    ON (u.id = fr.requester_id OR u.id = fr.receiver_id) AND u.id != :userId
                    WHERE
                    (fr.requester_id = :userId OR fr.receiver_id = :userId)
                    AND fr.status = 'ACCEPTED'
                    AND fr.active_flag IS NOT NULL
                    AND (LENGTH(:keyword) < 2 OR MATCH(u.nickname) AGAINST(:keyword IN BOOLEAN MODE))
                    AND u.deleted_at IS NULL
                    """,
            nativeQuery = true
    )
    Page<User> searchAcceptedFriendRequestByNickname(
            @Param("keyword") String keyword,
            @Param("userId") Long userId, // 나의 친구 목록을 열람 또는 다른 사람의 친구 목록을 열람.
            Pageable pageable
    );

    @Query(
            value = """
                    SELECT u.* FROM users AS u JOIN friend_request AS fr
                    ON u.id = fr.requester_id AND u.id != :userId
                    WHERE
                    fr.receiver_id = :userId
                    AND fr.status = 'PENDING'
                    AND fr.active_flag IS NOT NULL
                    AND (LENGTH(:keyword) < 2 OR MATCH(u.nickname) AGAINST(:keyword IN BOOLEAN MODE))
                    AND u.deleted_at IS NULL
                    """,
            countQuery = """
                    SELECT COUNT(*) FROM users AS u JOIN friend_request AS fr
                    ON u.id = fr.requester_id AND u.id != :userId
                    WHERE
                    fr.receiver_id = :userId
                    AND fr.status = 'PENDING'
                    AND fr.active_flag IS NOT NULL
                    AND (LENGTH(:keyword) < 2 OR MATCH(u.nickname) AGAINST(:keyword IN BOOLEAN MODE))
                    AND u.deleted_at IS NULL
                    """,
            nativeQuery = true
    )
    Page<User> searchPendingFriendRequestByNickname(
            @Param("keyword") String keyword,
            @Param("userId") Long loginUserId, // 나에게 전송된 친구 요청 목록을 열람
            Pageable pageable
    );

    @Query(
            value = """
                    SELECT u.* FROM users AS u JOIN friend_request AS fr
                    ON u.id = fr.receiver_id AND u.id != :userId
                    WHERE
                    fr.requester_id = :userId
                    AND fr.status = 'PENDING'
                    AND fr.active_flag IS NOT NULL
                    AND (LENGTH(:keyword) < 2 OR MATCH(u.nickname) AGAINST(:keyword IN BOOLEAN MODE))
                    AND u.deleted_at IS NULL
                    """,
            countQuery = """
                    SELECT COUNT(*) FROM users AS u JOIN friend_request AS fr
                    ON u.id = fr.receiver_id AND u.id != :userId
                    WHERE
                    fr.requester_id = :userId
                    AND fr.status = 'PENDING'
                    AND fr.active_flag IS NOT NULL
                    AND (LENGTH(:keyword) < 2 OR MATCH(u.nickname) AGAINST(:keyword IN BOOLEAN MODE))
                    AND u.deleted_at IS NULL
                    """,
            nativeQuery = true
    )
    Page<User> searchRequestedFriendRequestByNickname(
            @Param("keyword") String keyword,
            @Param("userId") Long loginUserId, // 내가 전송한 친구 요청 목록을 열람
            Pageable pageable
    );
}

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
            value = "SELECT * FROM users WHERE MATCH(nickname) AGAINST(:keyword IN BOOLEAN MODE) AND deleted_at IS NULL",
            countQuery = "SELECT COUNT(*) FROM users WHERE MATCH(nickname) AGAINST(:keyword IN BOOLEAN MODE) AND deleted_at IS NULL",
            nativeQuery = true
    )
    Page<User> searchByNickname(@Param("keyword") String keyword, Pageable pageable);
}

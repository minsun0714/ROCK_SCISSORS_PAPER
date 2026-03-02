package com.rsp.battle.user.persistence;

import com.rsp.battle.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}

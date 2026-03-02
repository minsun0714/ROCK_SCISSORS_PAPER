package com.rsp.battle.user.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_users_nickname", columnNames = {"nickname", "active_flag"}),
                @UniqueConstraint(name = "uq_users_email", columnNames = {"email", "oauth_provider", "active_flag"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(name = "profile_image_url", length = 255)
    private String profileImageUrl;

    @Column(name = "status_message", length = 100)
    private String statusMessage;

    @Column(name = "oauth_provider", nullable = false, length = 20)
    private String oauthProvider;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    @Column(name = "deleted_at")
    private Instant deletedAt;

    /**
     * generated column (STORED)
     * insert/update 불가
     */
    @Column(name = "active_flag", insertable = false, updatable = false)
    private Integer activeFlag;

    /* =========================
       생성 메서드
       ========================= */

    public static User createFromOAuth(String email,
                                       String nickname,
                                       String provider) {
        return User.builder()
                .email(email)
                .nickname(nickname)
                .oauthProvider(provider)
                .build();
    }

    // 상태 메시지 변경
    public void updateStatusMessage(String newStatusMessage) {
        this.statusMessage = newStatusMessage;
    }
}
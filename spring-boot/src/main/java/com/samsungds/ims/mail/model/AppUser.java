package com.samsungds.ims.mail.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "app_user")
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String username;
    @Column(nullable = false)
    private String password;
    @Column(unique = true, nullable = false)
    private String email;
    @Column(nullable = false)
    private boolean enabled;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToMany(fetch = FetchType.EAGER) // User 로드 시 Role도 즉시 로드
    @JoinTable(
            name = "app_user_role", // 조인 테이블 이름
            joinColumns = @JoinColumn(name = "user_id"), // User 엔티티의 ID
            inverseJoinColumns = @JoinColumn(name = "role_id") // Role 엔티티의 ID
    )
    private Set<AppRole> appRoles = new HashSet<>(); // 사용자가 가진 권한들
}
package com.samsungds.ims.mail.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "app_role")
public class AppRole implements GrantedAuthority {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name; // 예: ROLE_USER, ROLE_ADMIN

    @Override
    public String getAuthority() {
        return name; // Spring Security가 사용할 권한 문자열 (예: "ROLE_USER")
    }

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // hashCode와 equals 오버라이드 (Set에 사용될 때 중요)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppRole appRole = (AppRole) o;
        return Objects.equals(id, appRole.id) && Objects.equals(name, appRole.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
package com.spendsmart.auth.entity;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @JsonAlias("username")
    private String fullName;

    @Column(unique = true, nullable = false)
    private String email;

    @JsonAlias("password")
    private String passwordHash;

    private String currency;

    private String timezone;

    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    private AuthProvider provider;

    private Boolean isActive = true;

    private LocalDateTime createdAt;

    private Double monthlyBudget;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}

package com.spendsmart.auth.entity;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
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

    @JsonAlias("passwordHash")
    @JsonIgnore
    @Column(name = "password")
    private String password;

    private String currency;

    private String timezone;

    @Column(name = "status")
    private String status = "active";

    private LocalDateTime createdAt;

    private Double monthlyBudget;

    @Transient
    private String otp;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}

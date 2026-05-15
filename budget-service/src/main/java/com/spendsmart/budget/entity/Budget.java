package com.spendsmart.budget.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "budgets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long budgetId;

    private Long userId;

    private Double monthlyLimit;

    private Double spentAmount;

    private String currency;

    private Integer month;

    private Integer year;

    private Integer alertThreshold;

    private Integer categoryId;

    @Column(precision = 12, scale = 2)
    private BigDecimal limitAmount;

    private String name;

    @Enumerated(EnumType.STRING)
    private BudgetPeriod period;

    private Boolean isActive;

    private Boolean thresholdAlertSent;

    private Boolean limitAlertSent;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.isActive = true;
        if (this.spentAmount == null) {
            this.spentAmount = 0.0;
        }
        applyDefaults();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        applyDefaults();
    }

    private void applyDefaults() {
        if (this.alertThreshold == null) {
            this.alertThreshold = 50;
        }
        if (this.categoryId == null) {
            this.categoryId = 0;
        }
        if (this.limitAmount == null) {
            this.limitAmount = BigDecimal.valueOf(this.monthlyLimit == null ? 0.0 : this.monthlyLimit);
        }
        if (this.name == null || this.name.isBlank()) {
            this.name = "Monthly Budget";
        }
        if (this.period == null) {
            this.period = BudgetPeriod.MONTHLY;
        }
        if (this.thresholdAlertSent == null) {
            this.thresholdAlertSent = false;
        }
        if (this.limitAlertSent == null) {
            this.limitAlertSent = false;
        }
    }
}

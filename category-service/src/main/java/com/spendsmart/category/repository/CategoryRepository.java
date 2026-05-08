package com.spendsmart.category.repository;

import com.spendsmart.category.entity.Category;
import com.spendsmart.category.entity.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // 🔹 Get all categories by user
    List<Category> findByUserId(Long userId);

    // 🔹 Filter by type (EXPENSE / INCOME)
    List<Category> findByUserIdAndType(Long userId, CategoryType type);

}
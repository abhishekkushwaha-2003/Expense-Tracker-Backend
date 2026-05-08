package com.spendsmart.category.service;

import com.spendsmart.category.entity.Category;
import com.spendsmart.category.entity.CategoryType;

import java.util.List;

public interface CategoryService {

    Category addCategory(Category category);

    Category getCategoryById(Long id);

    List<Category> getCategoriesByUser(Long userId);

    List<Category> getCategoriesByType(Long userId, CategoryType type);

    Category updateCategory(Long id, Category category);

    void deleteCategory(Long id);
}
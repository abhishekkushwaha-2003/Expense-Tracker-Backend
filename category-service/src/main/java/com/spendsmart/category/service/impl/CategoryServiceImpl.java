package com.spendsmart.category.service.impl;

import com.spendsmart.category.entity.Category;
import com.spendsmart.category.entity.CategoryType;
import com.spendsmart.category.repository.CategoryRepository;
import com.spendsmart.category.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public Category addCategory(Category category) {
        return categoryRepository.save(category);
    }

    @Override
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }

    @Override
    public List<Category> getCategoriesByUser(Long userId) {
        return categoryRepository.findByUserId(userId);
    }

    @Override
    public List<Category> getCategoriesByType(Long userId, CategoryType type) {
        return categoryRepository.findByUserIdAndType(userId, type);
    }

    @Override
    public Category updateCategory(Long id, Category category) {

        Category existing = getCategoryById(id);

        existing.setName(category.getName());
        existing.setType(category.getType());
        existing.setIcon(category.getIcon());
        existing.setColor(category.getColor());

        return categoryRepository.save(existing);
    }

    @Override
    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }
}
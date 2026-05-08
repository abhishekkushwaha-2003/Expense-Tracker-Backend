package com.spendsmart.category.controller;

import com.spendsmart.category.entity.Category;
import com.spendsmart.category.entity.CategoryType;
import com.spendsmart.category.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    //  Add Category
    @PostMapping
    public Category addCategory(@RequestBody Category category) {
        return categoryService.addCategory(category);
    }

    //  Get by ID
    @GetMapping("/{id}")
    public Category getCategory(@PathVariable Long id) {
        return categoryService.getCategoryById(id);
    }

    //  Get all by user
    @GetMapping("/user/{userId}")
    public List<Category> getByUser(@PathVariable Long userId) {
        return categoryService.getCategoriesByUser(userId);
    }

    //  Get by type (EXPENSE / INCOME)
    @GetMapping("/user/{userId}/{type}")
    public List<Category> getByType(@PathVariable Long userId,
                                   @PathVariable CategoryType type) {
        return categoryService.getCategoriesByType(userId, type);
    }

    //  Update
    @PutMapping("/{id}")
    public Category updateCategory(@PathVariable Long id,
                                   @RequestBody Category category) {
        return categoryService.updateCategory(id, category);
    }

    //  Delete
    @DeleteMapping("/{id}")
    public String deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return "Deleted successfully";
    }
}
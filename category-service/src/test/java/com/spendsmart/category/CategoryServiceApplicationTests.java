package com.spendsmart.category;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.spendsmart.category.entity.Category;
import com.spendsmart.category.entity.CategoryType;
import com.spendsmart.category.repository.CategoryRepository;
import com.spendsmart.category.service.impl.CategoryServiceImpl;

@ExtendWith(MockitoExtension.class)
class CategoryServiceApplicationTests {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl service;

    @Test
    void addCategorySavesEntity() {
        Category category = Category.builder().name("Food").type(CategoryType.EXPENSE).build();
        when(categoryRepository.save(category)).thenReturn(category);

        Category saved = service.addCategory(category);

        assertEquals(category, saved);
    }

    @Test
    void updateCategoryCopiesEditableFields() {
        Category existing = Category.builder().categoryId(1L).name("Food").type(CategoryType.EXPENSE).build();
        Category update = Category.builder().name("Salary").type(CategoryType.INCOME).icon("wallet").color("green").build();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.save(existing)).thenReturn(existing);

        Category saved = service.updateCategory(1L, update);

        assertEquals("Salary", saved.getName());
        assertEquals(CategoryType.INCOME, saved.getType());
        assertEquals("wallet", saved.getIcon());
        assertEquals("green", saved.getColor());
    }

    @Test
    void getCategoryByIdThrowsWhenMissing() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.getCategoryById(99L));
        verify(categoryRepository).findById(99L);
    }
}


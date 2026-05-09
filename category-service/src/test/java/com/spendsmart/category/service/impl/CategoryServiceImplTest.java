package com.spendsmart.category.service.impl;

import com.spendsmart.category.entity.Category;
import com.spendsmart.category.entity.CategoryType;
import com.spendsmart.category.repository.CategoryRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category category;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .categoryId(1L)
                .userId(10L)
                .name("Food")
                .type(CategoryType.EXPENSE)
                .icon("utensils")
                .color("#ff0000")
                .build();
    }

    @Test
    void addCategorySavesCategory() {
        when(categoryRepository.save(category)).thenReturn(category);

        assertThat(categoryService.addCategory(category)).isSameAs(category);
        verify(categoryRepository).save(category);
    }

    @Test
    void getCategoryByIdReturnsCategoryWhenFound() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        assertThat(categoryService.getCategoryById(1L)).isSameAs(category);
    }

    @Test
    void getCategoryByIdThrowsWhenMissing() {
        when(categoryRepository.findById(44L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getCategoryById(44L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Category not found");
    }

    @Test
    void getCategoriesByUserDelegatesToRepository() {
        when(categoryRepository.findByUserId(10L)).thenReturn(List.of(category));

        assertThat(categoryService.getCategoriesByUser(10L)).containsExactly(category);
    }

    @Test
    void getCategoriesByTypeDelegatesToRepository() {
        when(categoryRepository.findByUserIdAndType(10L, CategoryType.EXPENSE)).thenReturn(List.of(category));

        assertThat(categoryService.getCategoriesByType(10L, CategoryType.EXPENSE)).containsExactly(category);
    }

    @Test
    void updateCategoryCopiesEditableFieldsAndSaves() {
        Category update = Category.builder()
                .name("Travel")
                .type(CategoryType.INCOME)
                .icon("plane")
                .color("#00ff00")
                .build();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(category)).thenReturn(category);

        Category result = categoryService.updateCategory(1L, update);

        assertThat(result.getName()).isEqualTo("Travel");
        assertThat(result.getType()).isEqualTo(CategoryType.INCOME);
        assertThat(result.getIcon()).isEqualTo("plane");
        assertThat(result.getColor()).isEqualTo("#00ff00");
        verify(categoryRepository).save(category);
    }

    @Test
    void deleteCategoryDelegatesToRepository() {
        categoryService.deleteCategory(1L);

        verify(categoryRepository).deleteById(1L);
    }
}

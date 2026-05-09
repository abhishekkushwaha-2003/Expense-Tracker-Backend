package com.spendsmart.category;

import com.spendsmart.category.controller.CategoryController;
import com.spendsmart.category.entity.Category;
import com.spendsmart.category.entity.CategoryType;
import com.spendsmart.category.service.CategoryService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CategoryLayerCoverageTest {

    @Test
    void controllerDelegatesToService() {
        CategoryService service = mock(CategoryService.class);
        CategoryController controller = new CategoryController();
        ReflectionTestUtils.setField(controller, "categoryService", service);
        Category category = Category.builder().categoryId(1L).userId(10L).type(CategoryType.EXPENSE).build();
        when(service.addCategory(category)).thenReturn(category);
        when(service.getCategoryById(1L)).thenReturn(category);
        when(service.getCategoriesByUser(10L)).thenReturn(List.of(category));
        when(service.getCategoriesByType(10L, CategoryType.EXPENSE)).thenReturn(List.of(category));
        when(service.updateCategory(1L, category)).thenReturn(category);

        assertSame(category, controller.addCategory(category));
        assertSame(category, controller.getCategory(1L));
        assertEquals(List.of(category), controller.getByUser(10L));
        assertEquals(List.of(category), controller.getByType(10L, CategoryType.EXPENSE));
        assertSame(category, controller.updateCategory(1L, category));
        assertEquals("Deleted successfully", controller.deleteCategory(1L));
        verify(service).deleteCategory(1L);
    }

    @Test
    void entityLifecycleSetsActiveAndUpdatedAt() {
        Category category = new Category();
        category.onCreate();
        assertNotNull(category.getCreatedAt());
        assertTrue(category.getIsActive());
        category.onUpdate();
        assertNotNull(category.getUpdatedAt());
        Category built = Category.builder().categoryId(1L).name("Food").type(CategoryType.EXPENSE).build();
        assertEquals("Food", built.getName());
        assertEquals(CategoryType.EXPENSE, built.getType());
    }
}
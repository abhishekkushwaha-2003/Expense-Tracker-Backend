package com.spendsmart.income;

import com.spendsmart.income.controller.IncomeController;
import com.spendsmart.income.entity.Income;
import com.spendsmart.income.service.IncomeService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IncomeLayerCoverageTest {

    @Test
    void controllerDelegatesToService() {
        IncomeService service = mock(IncomeService.class);
        IncomeController controller = new IncomeController();
        ReflectionTestUtils.setField(controller, "incomeService", service);
        Income income = new Income();
        income.setIncomeId(1L);
        when(service.addIncome(income)).thenReturn(income);
        when(service.getIncomeById(1L)).thenReturn(income);
        when(service.getIncomeByUser(10L)).thenReturn(List.of(income));
        when(service.updateIncome(1L, income)).thenReturn(income);

        assertSame(income, controller.addIncome(income));
        assertSame(income, controller.getIncome(1L));
        assertEquals(List.of(income), controller.getByUser(10L));
        assertSame(income, controller.updateIncome(1L, income));
        assertEquals("Deleted successfully", controller.deleteIncome(1L));
        verify(service).deleteIncome(1L);
    }

    @Test
    void entityAccessorsAndLifecycleWork() {
        Income income = new Income();
        LocalDateTime date = LocalDateTime.of(2026, 5, 9, 10, 0);
        income.setIncomeId(1L);
        income.setUserId(2L);
        income.setSource("Salary");
        income.setAmount(1000.0);
        income.setCurrency("INR");
        income.setDate(date);
        income.setNotes("note");
        income.setIsRecurring(true);
        income.onCreate();
        income.onUpdate();

        assertEquals(1L, income.getIncomeId());
        assertEquals(2L, income.getUserId());
        assertEquals("Salary", income.getSource());
        assertEquals(1000.0, income.getAmount());
        assertEquals("INR", income.getCurrency());
        assertEquals(date, income.getDate());
        assertEquals("note", income.getNotes());
        assertTrue(income.getIsRecurring());
        assertNotNull(income.getCreatedAt());
        assertNotNull(income.getUpdatedAt());
    }
}
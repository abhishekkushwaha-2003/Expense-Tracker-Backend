package com.spendsmart.expense;

import com.spendsmart.expense.config.RestConfig;
import com.spendsmart.expense.controller.ExpenseController;
import com.spendsmart.expense.entity.Expense;
import com.spendsmart.expense.entity.ExpenseType;
import com.spendsmart.expense.entity.PaymentMethod;
import com.spendsmart.expense.service.ExpenseService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExpenseLayerCoverageTest {

    @Test
    void controllerDelegatesToService() {
        ExpenseService service = mock(ExpenseService.class);
        ExpenseController controller = new ExpenseController();
        ReflectionTestUtils.setField(controller, "expenseService", service);
        Expense expense = Expense.builder().expenseId(1L).userId(10L).build();
        when(service.addExpense(expense)).thenReturn(expense);
        when(service.getExpenseById(1L)).thenReturn(expense);
        when(service.getExpensesByUser(10L)).thenReturn(List.of(expense));
        when(service.updateExpense(1L, expense)).thenReturn(expense);

        assertSame(expense, controller.addExpense(expense));
        assertSame(expense, controller.getExpense(1L));
        assertEquals(List.of(expense), controller.getByUser(10L));
        assertSame(expense, controller.updateExpense(1L, expense));
        assertEquals("Deleted successfully", controller.deleteExpense(1L));
        verify(service).deleteExpense(1L);
    }

    @Test
    void entityLifecycleDefaultsTypeAndBlankReceipt() {
        Expense expense = Expense.builder().receiptUrl(" ").build();
        expense.onCreate();
        assertNotNull(expense.getCreatedAt());
        assertEquals(ExpenseType.EXPENSE, expense.getType());
        assertNull(expense.getReceiptUrl());
        expense.setType(ExpenseType.SPLIT);
        expense.setPaymentMethod(PaymentMethod.CARD);
        expense.onUpdate();
        assertEquals(ExpenseType.SPLIT, expense.getType());
        assertEquals(PaymentMethod.CARD, expense.getPaymentMethod());
        assertNotNull(expense.getUpdatedAt());
        assertNotNull(new RestConfig().restTemplate());
    }
}
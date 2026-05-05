package com.spendsmart.income;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.spendsmart.income.entity.Income;
import com.spendsmart.income.repository.IncomeRepository;
import com.spendsmart.income.service.impl.IncomeServiceImpl;

@ExtendWith(MockitoExtension.class)
class IncomeServiceApplicationTests {

    @Mock
    private IncomeRepository incomeRepository;

    @InjectMocks
    private IncomeServiceImpl service;

    @Test
    void addIncomeSavesEntity() {
        Income income = new Income();
        ReflectionTestUtils.setField(income, "source", "Salary");
        ReflectionTestUtils.setField(income, "amount", 5000.0);
        when(incomeRepository.save(income)).thenReturn(income);

        Income saved = service.addIncome(income);

        assertEquals(income, saved);
    }

    @Test
    void updateIncomeCopiesEditableFields() {
        Income existing = new Income();
        ReflectionTestUtils.setField(existing, "incomeId", 1L);
        ReflectionTestUtils.setField(existing, "source", "Salary");
        ReflectionTestUtils.setField(existing, "amount", 2000.0);
        ReflectionTestUtils.setField(existing, "currency", "INR");

        Income update = new Income();
        ReflectionTestUtils.setField(update, "source", "Bonus");
        ReflectionTestUtils.setField(update, "amount", 500.0);
        ReflectionTestUtils.setField(update, "currency", "USD");
        ReflectionTestUtils.setField(update, "notes", "Quarterly");

        when(incomeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(incomeRepository.save(existing)).thenReturn(existing);

        Income saved = service.updateIncome(1L, update);

        assertEquals("Bonus", ReflectionTestUtils.getField(saved, "source"));
        assertEquals(500.0, ReflectionTestUtils.getField(saved, "amount"));
        assertEquals("USD", ReflectionTestUtils.getField(saved, "currency"));
        assertEquals("Quarterly", ReflectionTestUtils.getField(saved, "notes"));
    }
}

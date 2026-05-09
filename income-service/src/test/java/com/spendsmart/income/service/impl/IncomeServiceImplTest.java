package com.spendsmart.income.service.impl;

import com.spendsmart.income.entity.Income;
import com.spendsmart.income.repository.IncomeRepository;
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
class IncomeServiceImplTest {

    @Mock
    private IncomeRepository incomeRepository;

    @InjectMocks
    private IncomeServiceImpl incomeService;

    private Income income;

    @BeforeEach
    void setUp() {
        income = new Income();
        income.setIncomeId(1L);
        income.setUserId(10L);
        income.setSource("Salary");
        income.setAmount(5000.0);
        income.setCurrency("INR");
        income.setNotes("monthly");
    }

    @Test
    void addIncomeSavesIncome() {
        when(incomeRepository.save(income)).thenReturn(income);

        Income result = incomeService.addIncome(income);

        assertThat(result).isSameAs(income);
        verify(incomeRepository).save(income);
    }

    @Test
    void getIncomeByIdReturnsIncomeWhenFound() {
        when(incomeRepository.findById(1L)).thenReturn(Optional.of(income));

        assertThat(incomeService.getIncomeById(1L)).isSameAs(income);
    }

    @Test
    void getIncomeByIdThrowsWhenMissing() {
        when(incomeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> incomeService.getIncomeById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Income not found");
    }

    @Test
    void getIncomeByUserDelegatesToRepository() {
        when(incomeRepository.findByUserId(10L)).thenReturn(List.of(income));

        assertThat(incomeService.getIncomeByUser(10L)).containsExactly(income);
    }

    @Test
    void updateIncomeCopiesEditableFieldsAndSaves() {
        Income update = new Income();
        update.setSource("Freelance");
        update.setAmount(1200.0);
        update.setCurrency("USD");
        update.setNotes("project");
        when(incomeRepository.findById(1L)).thenReturn(Optional.of(income));
        when(incomeRepository.save(income)).thenReturn(income);

        Income result = incomeService.updateIncome(1L, update);

        assertThat(result.getSource()).isEqualTo("Freelance");
        assertThat(result.getAmount()).isEqualTo(1200.0);
        assertThat(result.getCurrency()).isEqualTo("USD");
        assertThat(result.getNotes()).isEqualTo("project");
        verify(incomeRepository).save(income);
    }

    @Test
    void deleteIncomeDelegatesToRepository() {
        incomeService.deleteIncome(1L);

        verify(incomeRepository).deleteById(1L);
    }
}

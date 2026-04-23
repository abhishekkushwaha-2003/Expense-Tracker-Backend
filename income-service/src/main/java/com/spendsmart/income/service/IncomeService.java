package com.spendsmart.income.service;

import com.spendsmart.income.entity.Income;

import java.util.List;

public interface IncomeService {

    Income addIncome(Income income);

    Income getIncomeById(Long id);

    List<Income> getIncomeByUser(Long userId);

    Income updateIncome(Long id, Income income);

    void deleteIncome(Long id);
}
package com.spendsmart.expense.service;

import com.spendsmart.expense.entity.Expense;

import java.util.List;

public interface ExpenseService {

    Expense addExpense(Expense expense);

    Expense getExpenseById(Long id);

    List<Expense> getExpensesByUser(Long userId);

    Expense updateExpense(Long id, Expense expense);

    void deleteExpense(Long id);
}
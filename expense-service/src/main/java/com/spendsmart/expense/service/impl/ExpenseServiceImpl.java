package com.spendsmart.expense.service.impl;

import com.spendsmart.expense.entity.Expense;
import com.spendsmart.expense.repository.ExpenseRepository;
import com.spendsmart.expense.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExpenseServiceImpl implements ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Override
    public Expense addExpense(Expense expense) {
        return expenseRepository.save(expense);
    }

    @Override
    public Expense getExpenseById(Long id) {
        return expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found"));
    }

    @Override
    public List<Expense> getExpensesByUser(Long userId) {
        return expenseRepository.findByUserId(userId);
    }

    @Override
    public Expense updateExpense(Long id, Expense expense) {

        Expense existing = getExpenseById(id);

        existing.setTitle(expense.getTitle());
        existing.setAmount(expense.getAmount());
        existing.setCategoryId(expense.getCategoryId());
        existing.setPaymentMethod(expense.getPaymentMethod());
        existing.setNotes(expense.getNotes());

        return expenseRepository.save(existing);
    }

    @Override
    public void deleteExpense(Long id) {
        expenseRepository.deleteById(id);
    }
}
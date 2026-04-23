package com.spendsmart.expense.controller;

import com.spendsmart.expense.entity.Expense;
import com.spendsmart.expense.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/expenses")
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    //  Add Expense
    @PostMapping
    public Expense addExpense(@RequestBody Expense expense) {
        return expenseService.addExpense(expense);
    }

    //  Get by ID
    @GetMapping("/{id}")
    public Expense getExpense(@PathVariable Long id) {
        return expenseService.getExpenseById(id);
    }

    //  Get all by user
    @GetMapping("/user/{userId}")
    public List<Expense> getByUser(@PathVariable Long userId) {
        return expenseService.getExpensesByUser(userId);
    }

    //  Update
    @PutMapping("/{id}")
    public Expense updateExpense(@PathVariable Long id,
                                 @RequestBody Expense expense) {
        return expenseService.updateExpense(id, expense);
    }

    //  Delete
    @DeleteMapping("/{id}")
    public String deleteExpense(@PathVariable Long id) {
        expenseService.deleteExpense(id);
        return "Deleted successfully";
    }
}
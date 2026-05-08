package com.spendsmart.budget.controller;

import com.spendsmart.budget.entity.Budget;
import com.spendsmart.budget.service.BudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/budgets")
public class BudgetController {

    @Autowired
    private BudgetService budgetService;

    //  Create Budget
    @PostMapping
    public Budget createBudget(@RequestBody Budget budget) {
        return budgetService.createBudget(budget);
    }

    //  Get by ID
    @GetMapping("/{id}")
    public Budget getBudget(@PathVariable Long id) {
        return budgetService.getBudgetById(id);
    }

    //  Get all by user
    @GetMapping("/user/{userId}")
    public List<Budget> getByUser(@PathVariable Long userId) {
        return budgetService.getBudgetsByUser(userId);
    }

    //  Get by month/year
    @GetMapping("/user/{userId}/{month}/{year}")
    public Budget getByMonth(@PathVariable Long userId,
                             @PathVariable Integer month,
                             @PathVariable Integer year) {
        return budgetService.getBudgetByMonth(userId, month, year);
    }

    //  Update
    @PutMapping("/{id}")
    public Budget updateBudget(@PathVariable Long id,
                               @RequestBody Budget budget) {
        return budgetService.updateBudget(id, budget);
    }

    //  Delete
    @DeleteMapping("/{id}")
    public String deleteBudget(@PathVariable Long id) {
        budgetService.deleteBudget(id);
        return "Deleted successfully";
    }
}
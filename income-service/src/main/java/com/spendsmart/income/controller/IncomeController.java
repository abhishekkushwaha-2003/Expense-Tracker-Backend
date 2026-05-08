package com.spendsmart.income.controller;

import com.spendsmart.income.entity.Income;
import com.spendsmart.income.service.IncomeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/income")
public class IncomeController {

    @Autowired
    private IncomeService incomeService;

    // ✅ Add Income
    @PostMapping
    public Income addIncome(@RequestBody Income income) {
        return incomeService.addIncome(income);
    }

    //  Get by ID
    @GetMapping("/{id}")
    public Income getIncome(@PathVariable Long id) {
        return incomeService.getIncomeById(id);
    }

    // Get all by user
    @GetMapping("/user/{userId}")
    public List<Income> getByUser(@PathVariable Long userId) {
        return incomeService.getIncomeByUser(userId);
    }

    //  Update
    @PutMapping("/{id}")
    public Income updateIncome(@PathVariable Long id,
                               @RequestBody Income income) {
        return incomeService.updateIncome(id, income);
    }

    //  Delete
    @DeleteMapping("/{id}")
    public String deleteIncome(@PathVariable Long id) {
        incomeService.deleteIncome(id);
        return "Deleted successfully";
    }
}
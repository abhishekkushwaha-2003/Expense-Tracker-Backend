package com.spendsmart.analytics.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    @Autowired
    private RestTemplate restTemplate;

    //  TOTAL EXPENSE
    @GetMapping("/expense/{userId}")
    public Double getTotalExpense(@PathVariable Long userId) {

        List<Map<String, Object>> expenses =
                restTemplate.getForObject(
                        "http://EXPENSE-SERVICE/expenses/user/" + userId,
                        List.class);

        double total = 0;
        for (Map<String, Object> e : expenses) {
            total += Double.parseDouble(e.get("amount").toString());
        }

        return total;
    }

    //  TOTAL INCOME
    @GetMapping("/income/{userId}")
    public Double getTotalIncome(@PathVariable Long userId) {

        List<Map<String, Object>> incomes =
                restTemplate.getForObject(
                        "http://INCOME-SERVICE/income/user/" + userId,
                        List.class);

        double total = 0;
        for (Map<String, Object> i : incomes) {
            total += Double.parseDouble(i.get("amount").toString());
        }

        return total;
    }

    // ✅SUMMARY
    @GetMapping("/summary/{userId}")
    public Map<String, Object> getSummary(@PathVariable Long userId) {

        Double income = getTotalIncome(userId);
        Double expense = getTotalExpense(userId);

        return Map.of(
                "totalIncome", income,
                "totalExpense", expense,
                "balance", income - expense
        );
    }
}
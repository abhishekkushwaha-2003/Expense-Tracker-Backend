package com.spendsmart.analytics.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.util.Collections;
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
                fetchRecords("http://EXPENSE-SERVICE/expenses/user/" + userId);

        double total = 0;
        for (Map<String, Object> e : expenses) {
            total += readAmount(e);
        }

        return total;
    }

    //  TOTAL INCOME
    @GetMapping("/income/{userId}")
    public Double getTotalIncome(@PathVariable Long userId) {

        List<Map<String, Object>> incomes =
                fetchRecords("http://INCOME-SERVICE/income/user/" + userId);

        double total = 0;
        for (Map<String, Object> i : incomes) {
            total += readAmount(i);
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

    private List<Map<String, Object>> fetchRecords(String url) {
        try {
            List<Map<String, Object>> records = restTemplate.getForObject(url, List.class);
            return records == null ? Collections.emptyList() : records;
        } catch (RestClientException ex) {
            return Collections.emptyList();
        }
    }

    private double readAmount(Map<String, Object> record) {
        Object amount = record.get("amount");
        if (amount == null) {
            return 0;
        }

        try {
            return Double.parseDouble(amount.toString());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}

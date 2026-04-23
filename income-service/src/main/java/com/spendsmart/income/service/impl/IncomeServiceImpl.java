package com.spendsmart.income.service.impl;

import com.spendsmart.income.entity.Income;
import com.spendsmart.income.repository.IncomeRepository;
import com.spendsmart.income.service.IncomeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IncomeServiceImpl implements IncomeService {

    @Autowired
    private IncomeRepository incomeRepository;

    @Override
    public Income addIncome(Income income) {
        return incomeRepository.save(income);
    }

    @Override
    public Income getIncomeById(Long id) {
        return incomeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Income not found"));
    }

    @Override
    public List<Income> getIncomeByUser(Long userId) {
        return incomeRepository.findByUserId(userId);
    }

    @Override
    public Income updateIncome(Long id, Income income) {

        Income existing = getIncomeById(id);

        existing.setSource(income.getSource());
        existing.setAmount(income.getAmount());
        existing.setCurrency(income.getCurrency());
        existing.setNotes(income.getNotes());

        return incomeRepository.save(existing);
    }

    @Override
    public void deleteIncome(Long id) {
        incomeRepository.deleteById(id);
    }
}
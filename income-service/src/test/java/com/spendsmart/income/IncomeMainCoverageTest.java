package com.spendsmart.income;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mockStatic;

class IncomeMainCoverageTest {

    @Test
    void mainDelegatesToSpringApplication() {
        String[] args = {"--server.port=0"};
        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            IncomeServiceApplication.main(args);
            springApplication.verify(() -> SpringApplication.run(IncomeServiceApplication.class, args));
        }
    }

    @Test
    void applicationCanBeConstructed() {
        assertNotNull(new IncomeServiceApplication());
    }
}
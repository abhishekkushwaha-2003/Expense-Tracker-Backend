package com.spendsmart.recurring.scheduler;

import com.spendsmart.recurring.entity.Recurring;
import com.spendsmart.recurring.entity.RecurringType;
import com.spendsmart.recurring.repository.RecurringRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
public class RecurringScheduler {

    @Autowired
    private RecurringRepository repository;

    @Autowired
    private RestTemplate restTemplate;

    @Scheduled(fixedRate = 60000) // every 1 minute
    public void processRecurring() {

        LocalDate today = LocalDate.now();

        System.out.println("🔄 Running scheduler at: " + LocalDateTime.now());

        List<Recurring> list =
                repository.findByNextExecutionDateAndActive(today, true);

        System.out.println("📌 Found recurring: " + list.size());

        for (Recurring r : list) {

            try {
                System.out.println("➡ Processing: " + r.getTitle());

                Map<String, Object> payload = Map.of(
                        "userId", r.getUserId(),
                        "title", r.getTitle(),
                        "amount", r.getAmount(),
                        "categoryId", r.getCategoryId(),
                        "type", r.getType().toString(),   // 🔥 FIX
                        "currency", "INR",                // 🔥 FIX
                        "paymentMethod", "AUTO",          // 🔥 FIX
                        "date", LocalDateTime.now().toString(),
                        "notes", "Auto-generated recurring",
                        "isRecurring", true
                );

                if (r.getType() == RecurringType.EXPENSE) {

                    restTemplate.postForObject(
                            "http://EXPENSE-SERVICE/expenses",
                            payload,
                            Object.class
                    );

                } else {

                    restTemplate.postForObject(
                            "http://INCOME-SERVICE/income",
                            payload,
                            Object.class
                    );
                }

                // ✅ update next execution
                r.setNextExecutionDate(r.getNextExecutionDate().plusMonths(1));
                repository.save(r);

                System.out.println("✅ Success: " + r.getTitle());

            } catch (Exception e) {

                System.out.println("❌ Error processing: " + r.getTitle());
                e.printStackTrace();
            }
        }
    }
}
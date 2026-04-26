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
                        "type", r.getType().toString(),
                        "currency", "INR",
                        "paymentMethod", "AUTO",
                        "date", LocalDateTime.now().toString(),
                        "notes", "Auto-generated recurring",
                        "isRecurring", true
                );

                // CALL EXPENSE / INCOME SERVICE
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

                // EMAIL TRIGGER
                sendEmail(r);

                // UPDATE NEXT DATE
                r.setNextExecutionDate(r.getNextExecutionDate().plusMonths(1));
                repository.save(r);

                System.out.println("✅ Success: " + r.getTitle());

            } catch (Exception e) {

                System.out.println("❌ Error processing: " + r.getTitle());
                e.printStackTrace();
            }
        }
    }

    // EMAIL METHOD
    private void sendEmail(Recurring r) {

        try {
            String url = "http://localhost:8088/notifications/email";

            Map<String, String> email = Map.of(
                    "to", "kushwahaabhishek10k@gmail.com",
                    "subject", "Recurring Transaction Alert",
                    "body", "Recurring executed:\n" +
                            "Title: " + r.getTitle() +
                            "\nAmount: ₹" + r.getAmount() +
                            "\nDate: " + LocalDateTime.now()
            );

            restTemplate.postForObject(url, email, String.class);

            System.out.println("📧 Email sent for: " + r.getTitle());

        } catch (Exception e) {
            System.out.println("❌ Email failed: " + r.getTitle());
            e.printStackTrace();
        }
    }
}
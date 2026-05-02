package com.spendsmart.recurring.scheduler;

import com.spendsmart.recurring.entity.Recurring;
import com.spendsmart.recurring.entity.RecurringType;
import com.spendsmart.recurring.repository.RecurringRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RecurringScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecurringScheduler.class);

    @Autowired
    private RecurringRepository repository;

    @Autowired
    private RestTemplate restTemplate;

    @Scheduled(fixedRate = 60000)
    public void processRecurring() {
        LocalDate today = LocalDate.now();
        sendDueSoonReminders(today);
        List<Recurring> list = repository.findByNextExecutionDateAndActive(today, true);

        for (Recurring recurring : list) {
            try {
                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("userId", recurring.getUserId());
                payload.put("title", recurring.getTitle());
                payload.put("amount", recurring.getAmount());
                if (recurring.getCategoryId() != null) {
                    payload.put("categoryId", recurring.getCategoryId());
                }
                payload.put("type", recurring.getType().toString());
                payload.put("currency", "INR");
                payload.put("paymentMethod", "BANK");
                payload.put("date", LocalDateTime.now().toString());
                payload.put("notes", "Auto-generated recurring");
                payload.put("isRecurring", true);

                if (recurring.getType() == RecurringType.EXPENSE) {
                    restTemplate.postForObject("http://EXPENSE-SERVICE/expenses", payload, Object.class);
                } else {
                    restTemplate.postForObject("http://INCOME-SERVICE/income", payload, Object.class);
                }

                sendEmail(recurring);
                recurring.setNextExecutionDate(calculateNextExecutionDate(recurring));
                recurring.setLastReminderSentAt(null);
                repository.save(recurring);
            } catch (Exception ex) {
                LOGGER.error("Recurring execution failed for {}", recurring.getTitle(), ex);
            }
        }
    }

    private void sendDueSoonReminders(LocalDate today) {
        LocalDate reminderDate = today.plusDays(3);
        List<Recurring> dueSoon = repository.findByNextExecutionDateAndActive(reminderDate, true);

        for (Recurring recurring : dueSoon) {
            if (today.equals(recurring.getLastReminderSentAt())) {
                continue;
            }

            try {
                String recipientEmail = fetchUserEmail(recurring.getUserId());
                Map<String, Object> notification = new LinkedHashMap<>();
                notification.put("recipientId", recurring.getUserId());
                notification.put("recipientEmail", recipientEmail);
                notification.put("type", "RECURRING_DUE");
                notification.put("severity", "INFO");
                notification.put("title", "Recurring transaction due in 3 days");
                notification.put(
                        "message",
                        String.format(
                                "Your recurring %s \"%s\" of INR %.2f is due on %s.",
                                recurring.getType().name().toLowerCase(),
                                recurring.getTitle(),
                                recurring.getAmount(),
                                recurring.getNextExecutionDate()
                        )
                );
                notification.put("relatedId", recurring.getId());
                notification.put("relatedType", "RECURRING");
                notification.put("emailEnabled", true);

                restTemplate.postForObject(
                        "http://NOTIFICATION-SERVICE/notifications/send",
                        notification,
                        Object.class
                );

                recurring.setLastReminderSentAt(today);
                repository.save(recurring);
            } catch (Exception ex) {
                LOGGER.error("Recurring reminder failed for {}", recurring.getTitle(), ex);
            }
        }
    }

    private LocalDate calculateNextExecutionDate(Recurring recurring) {
        LocalDate currentDate = recurring.getNextExecutionDate();
        String frequency = recurring.getFrequency() == null ? "MONTHLY" : recurring.getFrequency().trim().toUpperCase();

        return switch (frequency) {
            case "DAILY" -> currentDate.plusDays(1);
            case "WEEKLY" -> currentDate.plusWeeks(1);
            case "QUARTERLY" -> currentDate.plusMonths(3);
            case "YEARLY" -> currentDate.plusYears(1);
            default -> currentDate.plusMonths(1);
        };
    }

    private void sendEmail(Recurring recurring) {
        try {
            String recipientEmail = fetchUserEmail(recurring.getUserId());
            if (recipientEmail == null || recipientEmail.isBlank()) {
                return;
            }

            Map<String, String> email = Map.of(
                    "to", recipientEmail,
                    "subject", "Recurring Transaction Alert",
                    "body", "Recurring executed:\n"
                            + "Title: " + recurring.getTitle()
                            + "\nAmount: Rs " + recurring.getAmount()
                            + "\nDate: " + LocalDateTime.now()
            );

            restTemplate.postForObject(
                    "http://NOTIFICATION-SERVICE/notifications/email",
                    email,
                    String.class
            );
        } catch (Exception ex) {
            LOGGER.error("Recurring email failed for {}", recurring.getTitle(), ex);
        }
    }

    private String fetchUserEmail(Long userId) {
        if (userId == null) {
            return null;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> user = restTemplate.getForObject(
                "http://AUTH-SERVICE/auth/internal/users/{userId}",
                Map.class,
                userId
        );

        if (user == null) {
            return null;
        }

        Object email = user.get("email");
        return email == null ? null : email.toString();
    }
}

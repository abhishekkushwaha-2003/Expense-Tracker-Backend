package com.spendsmart.recurring.scheduler;

import com.spendsmart.recurring.entity.Recurring;
import com.spendsmart.recurring.entity.RecurringType;
import com.spendsmart.recurring.messaging.NotificationPublisher;
import com.spendsmart.recurring.repository.RecurringRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecurringSchedulerTest {

    @Mock
    private RecurringRepository repository;

    @Mock
    private NotificationPublisher notificationPublisher;

    private StubRestTemplate restTemplate;
    private RecurringScheduler scheduler;

    @BeforeEach
    void setUp() {
        restTemplate = new StubRestTemplate();
        scheduler = new RecurringScheduler();
        ReflectionTestUtils.setField(scheduler, "repository", repository);
        ReflectionTestUtils.setField(scheduler, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(scheduler, "notificationPublisher", notificationPublisher);
        ReflectionTestUtils.setField(scheduler, "asyncNotificationEnabled", false);
    }

    @Test
    void processRecurringPostsDueExpenseSendsEmailAndAdvancesMonthlyDate() {
        LocalDate today = LocalDate.now();
        Recurring recurring = recurring(RecurringType.EXPENSE, " monthly ", today);
        when(repository.findByNextExecutionDateAndActive(today.plusDays(3), true)).thenReturn(List.of());
        when(repository.findByNextExecutionDateAndActive(today, true)).thenReturn(List.of(recurring));
        restTemplate.userResponse = Map.of("email", "user@example.com");

        scheduler.processRecurring();

        assertThat(restTemplate.postUrls).contains(
                "http://EXPENSE-SERVICE/expenses",
                "http://NOTIFICATION-SERVICE/notifications/email"
        );
        assertThat(recurring.getNextExecutionDate()).isEqualTo(today.plusMonths(1));
        assertThat(recurring.getLastReminderSentAt()).isNull();
        verify(repository).save(recurring);
    }

    @Test
    void processRecurringPostsDueIncomeAndUsesWeeklyFrequency() {
        LocalDate today = LocalDate.now();
        Recurring recurring = recurring(RecurringType.INCOME, "WEEKLY", today);
        when(repository.findByNextExecutionDateAndActive(today.plusDays(3), true)).thenReturn(List.of());
        when(repository.findByNextExecutionDateAndActive(today, true)).thenReturn(List.of(recurring));
        restTemplate.userResponse = Map.of("email", "");

        scheduler.processRecurring();

        assertThat(restTemplate.postUrls).containsExactly("http://INCOME-SERVICE/income");
        assertThat(recurring.getNextExecutionDate()).isEqualTo(today.plusWeeks(1));
    }

    @Test
    void processRecurringSendsDueSoonReminderOnce() {
        LocalDate today = LocalDate.now();
        Recurring dueSoon = recurring(RecurringType.EXPENSE, "DAILY", today.plusDays(3));
        Recurring alreadyReminded = recurring(RecurringType.INCOME, "YEARLY", today.plusDays(3));
        alreadyReminded.setLastReminderSentAt(today);
        when(repository.findByNextExecutionDateAndActive(today.plusDays(3), true))
                .thenReturn(List.of(dueSoon, alreadyReminded));
        when(repository.findByNextExecutionDateAndActive(today, true)).thenReturn(List.of());
        restTemplate.userResponse = Map.of("email", "user@example.com");

        scheduler.processRecurring();

        assertThat(restTemplate.postUrls).containsExactly("http://NOTIFICATION-SERVICE/notifications/send");
        assertThat(dueSoon.getLastReminderSentAt()).isEqualTo(today);
        verify(repository).save(dueSoon);
    }

    @Test
    void processRecurringUsesAsyncPublisherWhenEnabled() {
        LocalDate today = LocalDate.now();
        Recurring dueSoon = recurring(RecurringType.EXPENSE, "QUARTERLY", today.plusDays(3));
        ReflectionTestUtils.setField(scheduler, "asyncNotificationEnabled", true);
        when(repository.findByNextExecutionDateAndActive(today.plusDays(3), true)).thenReturn(List.of(dueSoon));
        when(repository.findByNextExecutionDateAndActive(today, true)).thenReturn(List.of());

        scheduler.processRecurring();

        verify(notificationPublisher).publishNotification(any(Map.class));
        assertThat(restTemplate.postUrls).isEmpty();
    }

    @Test
    void processRecurringContinuesWhenRemotePostFails() {
        LocalDate today = LocalDate.now();
        Recurring recurring = recurring(RecurringType.EXPENSE, "YEARLY", today);
        restTemplate.throwOnPost = true;
        when(repository.findByNextExecutionDateAndActive(today.plusDays(3), true)).thenReturn(List.of());
        when(repository.findByNextExecutionDateAndActive(today, true)).thenReturn(List.of(recurring));

        scheduler.processRecurring();

        assertThat(recurring.getNextExecutionDate()).isEqualTo(today);
    }

    private Recurring recurring(RecurringType type, String frequency, LocalDate nextExecutionDate) {
        return Recurring.builder()
                .id(1L)
                .userId(10L)
                .title("Rent")
                .amount(1500.0)
                .categoryId(3L)
                .type(type)
                .frequency(frequency)
                .nextExecutionDate(nextExecutionDate)
                .active(true)
                .build();
    }

    private static class StubRestTemplate extends RestTemplate {
        private final List<String> postUrls = new ArrayList<>();
        private Map<String, Object> userResponse = Map.of();
        private boolean throwOnPost;

        @Override
        public <T> T postForObject(String url, Object request, Class<T> responseType, Object... uriVariables) {
            if (throwOnPost) {
                throw new IllegalStateException("remote unavailable");
            }
            postUrls.add(url);
            return null;
        }

        @Override
        public <T> T getForObject(String url, Class<T> responseType, Object... uriVariables) {
            return responseType.cast(userResponse);
        }
    }
}

package com.spendsmart.recurring.controller;

import com.spendsmart.recurring.entity.Recurring;
import com.spendsmart.recurring.entity.RecurringType;
import com.spendsmart.recurring.repository.RecurringRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecurringControllerTest {

    @Mock
    private RecurringRepository repository;

    private StubRestTemplate restTemplate;
    private RecurringController controller;

    @BeforeEach
    void setUp() {
        restTemplate = new StubRestTemplate();
        controller = new RecurringController();
        ReflectionTestUtils.setField(controller, "repository", repository);
        ReflectionTestUtils.setField(controller, "restTemplate", restTemplate);
    }

    @Test
    void createRejectsMissingStartDate() {
        Recurring recurring = Recurring.builder().userId(10L).build();

        var response = controller.create(recurring);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("startDate is required");
    }

    @Test
    void createRequiresActiveRecurringAccess() {
        restTemplate.response = Map.of("active", false);
        Recurring recurring = recurring(10L);

        var response = controller.create(recurring);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.PAYMENT_REQUIRED);
        assertThat(response.getBody()).isEqualTo("Recurring access requires an active payment.");
    }

    @Test
    void createSavesRecurringWhenAccessIsActive() {
        restTemplate.response = Map.of("active", true);
        Recurring recurring = recurring(10L);
        when(repository.save(recurring)).thenReturn(recurring);

        var response = controller.create(recurring);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(recurring.isActive()).isTrue();
        assertThat(recurring.getNextExecutionDate()).isEqualTo(recurring.getStartDate());
        assertThat(restTemplate.url).isEqualTo("http://PAYMENT-SERVICE/payments/recurring-access/user/{userId}/status");
    }

    @Test
    void createReturnsServerErrorWhenAccessLookupFails() {
        restTemplate.throwOnGet = true;
        Recurring recurring = recurring(10L);

        var response = controller.create(recurring);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(String.valueOf(response.getBody())).startsWith("Error:");
    }

    @Test
    void getAllAndGetByUserReadRepository() {
        Recurring first = recurring(10L);
        Recurring second = recurring(20L);
        when(repository.findAll()).thenReturn(List.of(first, second));

        assertThat(controller.getAll()).containsExactly(first, second);
        assertThat(controller.getByUser(10L)).containsExactly(first);
    }

    @Test
    void deleteDelegatesToRepository() {
        assertThat(controller.delete(1L)).isEqualTo("Deleted successfully");

        verify(repository).deleteById(1L);
    }

    private Recurring recurring(Long userId) {
        return Recurring.builder()
                .id(1L)
                .userId(userId)
                .title("Rent")
                .amount(1500.0)
                .type(RecurringType.EXPENSE)
                .startDate(LocalDate.of(2026, 5, 1))
                .frequency("MONTHLY")
                .build();
    }

    private static class StubRestTemplate extends RestTemplate {
        private Object response;
        private String url;
        private boolean throwOnGet;

        @Override
        public <T> T getForObject(String url, Class<T> responseType, Object... uriVariables) {
            this.url = url;
            if (throwOnGet) {
                throw new IllegalStateException("payment unavailable");
            }
            return responseType.cast(response);
        }
    }
}

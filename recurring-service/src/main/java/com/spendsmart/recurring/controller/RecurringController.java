package com.spendsmart.recurring.controller;

import com.spendsmart.recurring.entity.Recurring;
import com.spendsmart.recurring.repository.RecurringRepository;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/recurring")
public class RecurringController {

    @Autowired
    private RecurringRepository repository;

    @Autowired
    private RestTemplate restTemplate;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Recurring recurring) {
        try {
            if (recurring.getStartDate() == null) {
                return ResponseEntity.badRequest().body("startDate is required");
            }

            if (!hasRecurringAccess(recurring.getUserId())) {
                return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                        .body("Recurring access requires an active payment.");
            }

            recurring.setNextExecutionDate(recurring.getStartDate());
            recurring.setActive(true);

            Recurring saved = repository.save(recurring);
            return ResponseEntity.ok(saved);
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body("Error: " + ex.getMessage());
        }
    }

    @GetMapping
    public List<Recurring> getAll() {
        return repository.findAll();
    }

    @GetMapping("/user/{userId}")
    public List<Recurring> getByUser(@PathVariable Long userId) {
        return repository.findAll()
                .stream()
                .filter(recurring -> Objects.equals(recurring.getUserId(), userId))
                .toList();
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        repository.deleteById(id);
        return "Deleted successfully";
    }

    private boolean hasRecurringAccess(Long userId) {
        if (userId == null || userId <= 0) {
            return false;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.getForObject(
                "http://PAYMENT-SERVICE/payments/recurring-access/user/{userId}/status",
                Map.class,
                userId
        );

        return response != null && Boolean.TRUE.equals(response.get("active"));
    }
}

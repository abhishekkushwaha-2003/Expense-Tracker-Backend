package com.spendsmart.recurring.controller;

import com.spendsmart.recurring.entity.Recurring;
import com.spendsmart.recurring.repository.RecurringRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recurring")
public class RecurringController {

    @Autowired
    private RecurringRepository repository;

    //  CREATE RECURRING
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Recurring recurring) {

        try {
            
            System.out.println("Incoming Request → " + recurring);

            // safety check
            if (recurring.getStartDate() == null) {
                return ResponseEntity.badRequest().body("startDate is required");
            }

            recurring.setNextExecutionDate(recurring.getStartDate());
            recurring.setActive(true);

            Recurring saved = repository.save(recurring);

            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            e.printStackTrace(); 
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    //  GET ALL
    @GetMapping
    public List<Recurring> getAll() {
        return repository.findAll();
    }

    //  GET BY USER
    @GetMapping("/user/{userId}")
    public List<Recurring> getByUser(@PathVariable Long userId) {
        return repository.findAll()
                .stream()
                .filter(r -> r.getUserId().equals(userId))
                .toList();
    }

    //  DELETE
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        repository.deleteById(id);
        return "Deleted successfully";
    }
}
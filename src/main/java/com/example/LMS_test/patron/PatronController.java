package com.example.LMS_test.patron;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patrons")
@RequiredArgsConstructor
public class PatronController {
    private final PatronService patronService;

    @GetMapping
    public ResponseEntity<List<Patron>> getAllPatrons() {
        return ResponseEntity.ok(patronService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Patron> getPatronById(@PathVariable Long id) {
        return ResponseEntity.ok(patronService.getById(id));
    }

    @PostMapping
    public ResponseEntity<Patron> createPatron(@Valid @RequestBody PatronRequest patronRequest) {
        Patron patron = patronService.create(patronRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(patron);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Patron> updatePatron(@PathVariable Long id, @Valid @RequestBody PatronRequest patronRequest) {
        Patron updatedPatron = patronService.update(id, patronRequest);
        return ResponseEntity.ok(updatedPatron);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatron(@PathVariable Long id) {
        patronService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

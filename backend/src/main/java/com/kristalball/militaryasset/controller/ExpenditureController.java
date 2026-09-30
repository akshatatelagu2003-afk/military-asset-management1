package com.kristalball.militaryasset.controller;

import com.kristalball.militaryasset.entity.Expenditure;
import com.kristalball.militaryasset.service.ExpenditureService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing equipment expenditure records.
 * Base URL: /api/expenditures
 */
@RestController
@RequestMapping("/api/expenditures")
public class ExpenditureController {

    private final ExpenditureService expenditureService;

    public ExpenditureController(ExpenditureService expenditureService) {
        this.expenditureService = expenditureService;
    }

    /** GET /api/expenditures — returns all expenditure records */
    @GetMapping
    public ResponseEntity<List<Expenditure>> getAllExpenditures() {
        return ResponseEntity.ok(expenditureService.getAllExpenditures());
    }

    /** GET /api/expenditures/{id} — returns a single expenditure or 404 */
    @GetMapping("/{id}")
    public ResponseEntity<Expenditure> getExpenditureById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(expenditureService.getExpenditureById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** POST /api/expenditures — records a new expenditure, returns 201 CREATED */
    @PostMapping
    public ResponseEntity<Expenditure> createExpenditure(@RequestBody Expenditure expenditure) {
        Expenditure created = expenditureService.createExpenditure(expenditure);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /** PUT /api/expenditures/{id} — updates an expenditure record, returns 200 OK or 404 */
    @PutMapping("/{id}")
    public ResponseEntity<Expenditure> updateExpenditure(@PathVariable Long id,
                                                         @RequestBody Expenditure expenditure) {
        try {
            return ResponseEntity.ok(expenditureService.updateExpenditure(id, expenditure));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** DELETE /api/expenditures/{id} — deletes an expenditure, returns 204 NO CONTENT or 404 */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpenditure(@PathVariable Long id) {
        try {
            expenditureService.deleteExpenditure(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

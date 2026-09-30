package com.kristalball.militaryasset.controller;

import com.kristalball.militaryasset.entity.Assignment;
import com.kristalball.militaryasset.service.AssignmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing equipment assignments to personnel.
 * Base URL: /api/assignments
 */
@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    /** GET /api/assignments — returns all assignment records */
    @GetMapping
    public ResponseEntity<List<Assignment>> getAllAssignments() {
        return ResponseEntity.ok(assignmentService.getAllAssignments());
    }

    /** GET /api/assignments/{id} — returns a single assignment or 404 */
    @GetMapping("/{id}")
    public ResponseEntity<Assignment> getAssignmentById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(assignmentService.getAssignmentById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** POST /api/assignments — records a new assignment, returns 201 CREATED */
    @PostMapping
    public ResponseEntity<Assignment> createAssignment(@RequestBody Assignment assignment) {
        Assignment created = assignmentService.createAssignment(assignment);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /** PUT /api/assignments/{id} — updates an assignment record, returns 200 OK or 404 */
    @PutMapping("/{id}")
    public ResponseEntity<Assignment> updateAssignment(@PathVariable Long id,
                                                       @RequestBody Assignment assignment) {
        try {
            return ResponseEntity.ok(assignmentService.updateAssignment(id, assignment));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** DELETE /api/assignments/{id} — deletes an assignment, returns 204 NO CONTENT or 404 */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable Long id) {
        try {
            assignmentService.deleteAssignment(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

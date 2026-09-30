package com.kristalball.militaryasset.controller;

import com.kristalball.militaryasset.entity.Base;
import com.kristalball.militaryasset.service.BaseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing military bases.
 * Base URL: /api/bases
 */
@RestController
@RequestMapping("/api/bases")
public class BaseController {

    private final BaseService baseService;

    public BaseController(BaseService baseService) {
        this.baseService = baseService;
    }

    /** GET /api/bases — returns all bases */
    @GetMapping
    public ResponseEntity<List<Base>> getAllBases() {
        return ResponseEntity.ok(baseService.getAllBases());
    }

    /** GET /api/bases/{id} — returns a single base or 404 */
    @GetMapping("/{id}")
    public ResponseEntity<Base> getBaseById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(baseService.getBaseById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** POST /api/bases — creates a new base, returns 201 CREATED */
    @PostMapping
    public ResponseEntity<Base> createBase(@RequestBody Base base) {
        Base created = baseService.createBase(base);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /** PUT /api/bases/{id} — updates an existing base, returns 200 OK or 404 */
    @PutMapping("/{id}")
    public ResponseEntity<Base> updateBase(@PathVariable Long id, @RequestBody Base base) {
        try {
            return ResponseEntity.ok(baseService.updateBase(id, base));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** DELETE /api/bases/{id} — deletes a base, returns 204 NO CONTENT or 404 */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBase(@PathVariable Long id) {
        try {
            baseService.deleteBase(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

package com.kristalball.militaryasset.controller;

import com.kristalball.militaryasset.entity.Equipment;
import com.kristalball.militaryasset.service.EquipmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing equipment types.
 * Base URL: /api/equipment
 */
@RestController
@RequestMapping("/api/equipment")
public class EquipmentController {

    private final EquipmentService equipmentService;

    public EquipmentController(EquipmentService equipmentService) {
        this.equipmentService = equipmentService;
    }

    /** GET /api/equipment — returns all equipment types */
    @GetMapping
    public ResponseEntity<List<Equipment>> getAllEquipment() {
        return ResponseEntity.ok(equipmentService.getAllEquipment());
    }

    /** GET /api/equipment/{id} — returns a single equipment record or 404 */
    @GetMapping("/{id}")
    public ResponseEntity<Equipment> getEquipmentById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(equipmentService.getEquipmentById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** POST /api/equipment — creates new equipment, returns 201 CREATED */
    @PostMapping
    public ResponseEntity<Equipment> createEquipment(@RequestBody Equipment equipment) {
        Equipment created = equipmentService.createEquipment(equipment);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /** PUT /api/equipment/{id} — updates existing equipment, returns 200 OK or 404 */
    @PutMapping("/{id}")
    public ResponseEntity<Equipment> updateEquipment(@PathVariable Long id,
                                                     @RequestBody Equipment equipment) {
        try {
            return ResponseEntity.ok(equipmentService.updateEquipment(id, equipment));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** DELETE /api/equipment/{id} — deletes equipment, returns 204 NO CONTENT or 404 */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEquipment(@PathVariable Long id) {
        try {
            equipmentService.deleteEquipment(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

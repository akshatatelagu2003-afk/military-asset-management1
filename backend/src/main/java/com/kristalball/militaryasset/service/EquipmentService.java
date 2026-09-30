package com.kristalball.militaryasset.service;

import com.kristalball.militaryasset.entity.Equipment;
import com.kristalball.militaryasset.repository.EquipmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for the Equipment entity.
 * Provides CRUD operations for military equipment types.
 */
@Service
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;

    public EquipmentService(EquipmentRepository equipmentRepository) {
        this.equipmentRepository = equipmentRepository;
    }

    /** Return all equipment records. */
    public List<Equipment> getAllEquipment() {
        return equipmentRepository.findAll();
    }

    /**
     * Return a single equipment record by ID.
     * Throws RuntimeException if not found.
     */
    public Equipment getEquipmentById(Long id) {
        return equipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipment not found with id: " + id));
    }

    /** Save new equipment to the database. */
    public Equipment createEquipment(Equipment equipment) {
        return equipmentRepository.save(equipment);
    }

    /**
     * Update an existing equipment record.
     * Throws RuntimeException if not found.
     */
    public Equipment updateEquipment(Long id, Equipment updatedEquipment) {
        Equipment existing = getEquipmentById(id); // throws if not found
        existing.setName(updatedEquipment.getName());
        existing.setType(updatedEquipment.getType());
        existing.setDescription(updatedEquipment.getDescription());
        return equipmentRepository.save(existing);
    }

    /**
     * Delete equipment by ID.
     * Throws RuntimeException if not found.
     */
    public void deleteEquipment(Long id) {
        if (!equipmentRepository.existsById(id)) {
            throw new RuntimeException("Cannot delete. Equipment not found with id: " + id);
        }
        equipmentRepository.deleteById(id);
    }
}

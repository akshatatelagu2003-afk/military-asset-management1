package com.kristalball.militaryasset.service;

import com.kristalball.militaryasset.entity.Base;
import com.kristalball.militaryasset.repository.BaseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for the Base entity.
 * Provides CRUD operations for military bases.
 */
@Service
public class BaseService {

    private final BaseRepository baseRepository;

    // Constructor injection — Spring will inject the repository automatically.
    public BaseService(BaseRepository baseRepository) {
        this.baseRepository = baseRepository;
    }

    /** Return all bases in the database. */
    public List<Base> getAllBases() {
        return baseRepository.findAll();
    }

    /**
     * Return a single base by its ID.
     * Throws a RuntimeException if no base is found with that ID.
     */
    public Base getBaseById(Long id) {
        return baseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Base not found with id: " + id));
    }

    /** Save a new base to the database. */
    public Base createBase(Base base) {
        return baseRepository.save(base);
    }

    /**
     * Update an existing base.
     * Throws a RuntimeException if the base with the given ID does not exist.
     */
    public Base updateBase(Long id, Base updatedBase) {
        Base existing = getBaseById(id); // will throw if not found
        existing.setName(updatedBase.getName());
        existing.setLocation(updatedBase.getLocation());
        return baseRepository.save(existing);
    }

    /**
     * Delete a base by its ID.
     * Throws a RuntimeException if the base does not exist.
     */
    public void deleteBase(Long id) {
        if (!baseRepository.existsById(id)) {
            throw new RuntimeException("Cannot delete. Base not found with id: " + id);
        }
        baseRepository.deleteById(id);
    }
}

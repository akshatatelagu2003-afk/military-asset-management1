package com.kristalball.militaryasset.service;

import com.kristalball.militaryasset.config.CurrentUserService;
import com.kristalball.militaryasset.entity.Purchase;
import com.kristalball.militaryasset.repository.PurchaseRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for Purchase transactions.
 *
 * RBAC enforcement:
 * - ADMIN           : full CRUD, all bases.
 * - BASE_COMMANDER  : can only read/write purchases that belong to their own base.
 *                     The base is taken from the JWT token (CurrentUserService),
 *                     NEVER from a request parameter.
 * - LOGISTICS_OFFICER: read + create purchases; update/delete are allowed
 *                     (they co-manage supply logistics). No base restriction applied
 *                     at this layer because the SecurityConfig already limits access.
 */
@Service
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final CurrentUserService currentUserService;

    public PurchaseService(PurchaseRepository purchaseRepository,
                           CurrentUserService currentUserService) {
        this.purchaseRepository = purchaseRepository;
        this.currentUserService = currentUserService;
    }

    /**
     * Return purchases. BASE_COMMANDER sees only their base's purchases.
     * ADMIN and LOGISTICS_OFFICER see all purchases.
     */
    public List<Purchase> getAllPurchases() {
        List<Purchase> all = purchaseRepository.findAll();
        if (currentUserService.isBaseCommander()) {
            Long myBaseId = currentUserService.getCurrentUserBaseId();
            return all.stream()
                    .filter(p -> p.getBase() != null && p.getBase().getId().equals(myBaseId))
                    .collect(Collectors.toList());
        }
        return all;
    }

    /**
     * Return a single purchase by ID.
     * BASE_COMMANDER may only access purchases for their own base.
     */
    public Purchase getPurchaseById(Long id) {
        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase not found with id: " + id));
        assertBaseAccess(purchase.getBase() != null ? purchase.getBase().getId() : null);
        return purchase;
    }

    /**
     * Create a purchase.
     * BASE_COMMANDER: the base is overridden to their own base to prevent spoofing.
     * createdBy: always set from the JWT-authenticated user — never from the request body.
     */
    public Purchase createPurchase(Purchase purchase) {
        // Always set createdBy from the authenticated user (never trust the request body)
        purchase.setCreatedBy(currentUserService.getCurrentUser());

        if (currentUserService.isBaseCommander()) {
            // Force base to the caller's own base — ignore whatever was in the request body
            Long myBaseId = currentUserService.getCurrentUserBaseId();
            com.kristalball.militaryasset.entity.Base myBase =
                    new com.kristalball.militaryasset.entity.Base();
            myBase.setId(myBaseId);
            purchase.setBase(myBase);
        }
        return purchaseRepository.save(purchase);
    }

    /**
     * Update a purchase. BASE_COMMANDER can only update purchases for their own base.
     */
    public Purchase updatePurchase(Long id, Purchase updatedPurchase) {
        Purchase existing = getPurchaseById(id); // also enforces base access
        existing.setEquipment(updatedPurchase.getEquipment());
        existing.setQuantity(updatedPurchase.getQuantity());
        existing.setPurchaseDate(updatedPurchase.getPurchaseDate());
        existing.setCreatedBy(updatedPurchase.getCreatedBy());
        // Base is intentionally NOT updated to prevent reassignment to another base
        return purchaseRepository.save(existing);
    }

    /**
     * Delete a purchase. BASE_COMMANDER can only delete purchases for their own base.
     */
    public void deletePurchase(Long id) {
        getPurchaseById(id); // also enforces base access
        purchaseRepository.deleteById(id);
    }

    /**
     * Throws an AccessDeniedException (403) if the caller is a BASE_COMMANDER
     * trying to access a record that doesn't belong to their base.
     */
    private void assertBaseAccess(Long recordBaseId) {
        if (currentUserService.isBaseCommander()) {
            Long myBaseId = currentUserService.getCurrentUserBaseId();
            if (recordBaseId == null || !recordBaseId.equals(myBaseId)) {
                throw new org.springframework.security.access.AccessDeniedException(
                        "Access denied: this record belongs to a different base.");
            }
        }
    }
}

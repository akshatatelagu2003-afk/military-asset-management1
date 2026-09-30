package com.kristalball.militaryasset.controller;

import com.kristalball.militaryasset.entity.Purchase;
import com.kristalball.militaryasset.service.PurchaseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing equipment purchase transactions.
 * Base URL: /api/purchases
 */
@RestController
@RequestMapping("/api/purchases")
public class PurchaseController {

    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    /** GET /api/purchases — returns all purchases */
    @GetMapping
    public ResponseEntity<List<Purchase>> getAllPurchases() {
        return ResponseEntity.ok(purchaseService.getAllPurchases());
    }

    /** GET /api/purchases/{id} — returns a single purchase or 404 */
    @GetMapping("/{id}")
    public ResponseEntity<Purchase> getPurchaseById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(purchaseService.getPurchaseById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** POST /api/purchases — records a new purchase, returns 201 CREATED */
    @PostMapping
    public ResponseEntity<Purchase> createPurchase(@RequestBody Purchase purchase) {
        Purchase created = purchaseService.createPurchase(purchase);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /** PUT /api/purchases/{id} — updates a purchase record, returns 200 OK or 404 */
    @PutMapping("/{id}")
    public ResponseEntity<Purchase> updatePurchase(@PathVariable Long id,
                                                   @RequestBody Purchase purchase) {
        try {
            return ResponseEntity.ok(purchaseService.updatePurchase(id, purchase));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** DELETE /api/purchases/{id} — deletes a purchase, returns 204 NO CONTENT or 404 */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePurchase(@PathVariable Long id) {
        try {
            purchaseService.deletePurchase(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

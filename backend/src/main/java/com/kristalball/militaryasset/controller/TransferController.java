package com.kristalball.militaryasset.controller;

import com.kristalball.militaryasset.entity.Transfer;
import com.kristalball.militaryasset.service.TransferService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing equipment transfers between bases.
 * Base URL: /api/transfers
 */
@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    /** GET /api/transfers — returns all transfers */
    @GetMapping
    public ResponseEntity<List<Transfer>> getAllTransfers() {
        return ResponseEntity.ok(transferService.getAllTransfers());
    }

    /** GET /api/transfers/{id} — returns a single transfer or 404 */
    @GetMapping("/{id}")
    public ResponseEntity<Transfer> getTransferById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(transferService.getTransferById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** POST /api/transfers — records a new transfer, returns 201 CREATED */
    @PostMapping
    public ResponseEntity<Transfer> createTransfer(@RequestBody Transfer transfer) {
        Transfer created = transferService.createTransfer(transfer);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /** PUT /api/transfers/{id} — updates a transfer record, returns 200 OK or 404 */
    @PutMapping("/{id}")
    public ResponseEntity<Transfer> updateTransfer(@PathVariable Long id,
                                                   @RequestBody Transfer transfer) {
        try {
            return ResponseEntity.ok(transferService.updateTransfer(id, transfer));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** DELETE /api/transfers/{id} — deletes a transfer, returns 204 NO CONTENT or 404 */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransfer(@PathVariable Long id) {
        try {
            transferService.deleteTransfer(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

package com.kristalball.militaryasset.service;

import com.kristalball.militaryasset.config.CurrentUserService;
import com.kristalball.militaryasset.entity.Transfer;
import com.kristalball.militaryasset.repository.TransferRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for Transfer transactions.
 *
 * RBAC enforcement:
 * - ADMIN            : full CRUD, all bases.
 * - BASE_COMMANDER   : can see/create transfers where their base is either
 *                      fromBase OR toBase. Cannot access transfers between
 *                      two other bases. Base is taken from JWT, never from request.
 * - LOGISTICS_OFFICER: read + create transfers (no base restriction at this layer).
 */
@Service
public class TransferService {

    private final TransferRepository transferRepository;
    private final CurrentUserService currentUserService;

    public TransferService(TransferRepository transferRepository,
                           CurrentUserService currentUserService) {
        this.transferRepository = transferRepository;
        this.currentUserService = currentUserService;
    }

    /**
     * Return transfers. BASE_COMMANDER sees only transfers involving their base
     * (either as source or destination).
     */
    public List<Transfer> getAllTransfers() {
        List<Transfer> all = transferRepository.findAll();
        if (currentUserService.isBaseCommander()) {
            Long myBaseId = currentUserService.getCurrentUserBaseId();
            return all.stream()
                    .filter(t -> involvesBase(t, myBaseId))
                    .collect(Collectors.toList());
        }
        return all;
    }

    /**
     * Return a single transfer. BASE_COMMANDER may only access transfers involving
     * their own base.
     */
    public Transfer getTransferById(Long id) {
        Transfer transfer = transferRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transfer not found with id: " + id));
        if (currentUserService.isBaseCommander()) {
            Long myBaseId = currentUserService.getCurrentUserBaseId();
            if (!involvesBase(transfer, myBaseId)) {
                throw new org.springframework.security.access.AccessDeniedException(
                        "Access denied: this transfer does not involve your base.");
            }
        }
        return transfer;
    }

    /**
     * Create a transfer. For BASE_COMMANDER, the fromBase is forced to their own base
     * to prevent spoofing the source.
     * createdBy: always set from the JWT-authenticated user — never from the request body.
     */
    public Transfer createTransfer(Transfer transfer) {
        // Always set createdBy from the authenticated user (never trust the request body)
        transfer.setCreatedBy(currentUserService.getCurrentUser());

        if (currentUserService.isBaseCommander()) {
            Long myBaseId = currentUserService.getCurrentUserBaseId();
            com.kristalball.militaryasset.entity.Base myBase =
                    new com.kristalball.militaryasset.entity.Base();
            myBase.setId(myBaseId);
            transfer.setFromBase(myBase); // BASE_COMMANDER can only send FROM their base
        }
        return transferRepository.save(transfer);
    }

    /** Update a transfer. BASE_COMMANDER can only update transfers involving their base. */
    public Transfer updateTransfer(Long id, Transfer updatedTransfer) {
        Transfer existing = getTransferById(id); // also enforces base access
        existing.setEquipment(updatedTransfer.getEquipment());
        existing.setQuantity(updatedTransfer.getQuantity());
        existing.setTransferDate(updatedTransfer.getTransferDate());
        existing.setCreatedBy(updatedTransfer.getCreatedBy());
        // fromBase and toBase are NOT updated — prevents reassignment
        return transferRepository.save(existing);
    }

    /** Delete a transfer. BASE_COMMANDER can only delete transfers involving their base. */
    public void deleteTransfer(Long id) {
        getTransferById(id); // also enforces base access
        transferRepository.deleteById(id);
    }

    /** Returns true if the transfer involves the given base (as source or destination). */
    private boolean involvesBase(Transfer t, Long baseId) {
        boolean fromMatch = t.getFromBase() != null && t.getFromBase().getId().equals(baseId);
        boolean toMatch   = t.getToBase()   != null && t.getToBase().getId().equals(baseId);
        return fromMatch || toMatch;
    }
}

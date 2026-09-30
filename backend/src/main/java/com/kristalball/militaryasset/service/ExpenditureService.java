package com.kristalball.militaryasset.service;

import com.kristalball.militaryasset.config.CurrentUserService;
import com.kristalball.militaryasset.entity.Expenditure;
import com.kristalball.militaryasset.repository.ExpenditureRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for Expenditure records.
 *
 * RBAC enforcement:
 * - ADMIN           : full CRUD, all bases.
 * - BASE_COMMANDER  : can only read/write expenditures for their own base.
 *                     Base is always taken from JWT token, never from request.
 * - LOGISTICS_OFFICER: no access — blocked at SecurityConfig level.
 */
@Service
public class ExpenditureService {

    private final ExpenditureRepository expenditureRepository;
    private final CurrentUserService currentUserService;

    public ExpenditureService(ExpenditureRepository expenditureRepository,
                              CurrentUserService currentUserService) {
        this.expenditureRepository = expenditureRepository;
        this.currentUserService = currentUserService;
    }

    /** Return expenditures. BASE_COMMANDER sees only their base's expenditures. */
    public List<Expenditure> getAllExpenditures() {
        List<Expenditure> all = expenditureRepository.findAll();
        if (currentUserService.isBaseCommander()) {
            Long myBaseId = currentUserService.getCurrentUserBaseId();
            return all.stream()
                    .filter(e -> e.getBase() != null && e.getBase().getId().equals(myBaseId))
                    .collect(Collectors.toList());
        }
        return all;
    }

    /** Return a single expenditure. BASE_COMMANDER may only access their own base. */
    public Expenditure getExpenditureById(Long id) {
        Expenditure expenditure = expenditureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expenditure not found with id: " + id));
        assertBaseAccess(expenditure.getBase() != null ? expenditure.getBase().getId() : null);
        return expenditure;
    }

    /** Create an expenditure. BASE_COMMANDER's base is forced from JWT token.
     *  createdBy is always set from the authenticated user — never from the request body. */
    public Expenditure createExpenditure(Expenditure expenditure) {
        // Always set from the authenticated caller — never trust the request body
        expenditure.setCreatedBy(currentUserService.getCurrentUser());

        if (currentUserService.isBaseCommander()) {
            Long myBaseId = currentUserService.getCurrentUserBaseId();
            com.kristalball.militaryasset.entity.Base myBase =
                    new com.kristalball.militaryasset.entity.Base();
            myBase.setId(myBaseId);
            expenditure.setBase(myBase);
        }
        return expenditureRepository.save(expenditure);
    }

    /** Update an expenditure. BASE_COMMANDER can only update their own base's records. */
    public Expenditure updateExpenditure(Long id, Expenditure updatedExpenditure) {
        Expenditure existing = getExpenditureById(id); // enforces base access
        existing.setEquipment(updatedExpenditure.getEquipment());
        existing.setQuantity(updatedExpenditure.getQuantity());
        existing.setReason(updatedExpenditure.getReason());
        existing.setExpendedDate(updatedExpenditure.getExpendedDate());
        existing.setCreatedBy(updatedExpenditure.getCreatedBy());
        // Base is NOT updated — prevents reassignment to another base
        return expenditureRepository.save(existing);
    }

    /** Delete an expenditure. BASE_COMMANDER can only delete their own base's records. */
    public void deleteExpenditure(Long id) {
        getExpenditureById(id); // enforces base access
        expenditureRepository.deleteById(id);
    }

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

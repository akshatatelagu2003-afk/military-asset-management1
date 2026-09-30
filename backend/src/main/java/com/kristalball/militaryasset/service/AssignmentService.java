package com.kristalball.militaryasset.service;

import com.kristalball.militaryasset.config.CurrentUserService;
import com.kristalball.militaryasset.entity.Assignment;
import com.kristalball.militaryasset.repository.AssignmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for Assignment records.
 *
 * RBAC enforcement:
 * - ADMIN           : full CRUD, all bases.
 * - BASE_COMMANDER  : can only read/write assignments for their own base.
 *                     Base is always taken from JWT token, never from request.
 * - LOGISTICS_OFFICER: no access — blocked at SecurityConfig level.
 */
@Service
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final CurrentUserService currentUserService;

    public AssignmentService(AssignmentRepository assignmentRepository,
                             CurrentUserService currentUserService) {
        this.assignmentRepository = assignmentRepository;
        this.currentUserService = currentUserService;
    }

    /** Return assignments. BASE_COMMANDER sees only their base's assignments. */
    public List<Assignment> getAllAssignments() {
        List<Assignment> all = assignmentRepository.findAll();
        if (currentUserService.isBaseCommander()) {
            Long myBaseId = currentUserService.getCurrentUserBaseId();
            return all.stream()
                    .filter(a -> a.getBase() != null && a.getBase().getId().equals(myBaseId))
                    .collect(Collectors.toList());
        }
        return all;
    }

    /** Return a single assignment. BASE_COMMANDER may only access their own base. */
    public Assignment getAssignmentById(Long id) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assignment not found with id: " + id));
        assertBaseAccess(assignment.getBase() != null ? assignment.getBase().getId() : null);
        return assignment;
    }

    /** Create an assignment. BASE_COMMANDER's base is forced from JWT token.
     *  createdBy is always set from the authenticated user — never from the request body. */
    public Assignment createAssignment(Assignment assignment) {
        // Always set from the authenticated caller — never trust the request body
        assignment.setCreatedBy(currentUserService.getCurrentUser());

        if (currentUserService.isBaseCommander()) {
            Long myBaseId = currentUserService.getCurrentUserBaseId();
            com.kristalball.militaryasset.entity.Base myBase =
                    new com.kristalball.militaryasset.entity.Base();
            myBase.setId(myBaseId);
            assignment.setBase(myBase);
        }
        return assignmentRepository.save(assignment);
    }

    /** Update an assignment. BASE_COMMANDER can only update their own base's records. */
    public Assignment updateAssignment(Long id, Assignment updatedAssignment) {
        Assignment existing = getAssignmentById(id); // enforces base access
        existing.setEquipment(updatedAssignment.getEquipment());
        existing.setPersonnelName(updatedAssignment.getPersonnelName());
        existing.setQuantity(updatedAssignment.getQuantity());
        existing.setAssignedDate(updatedAssignment.getAssignedDate());
        existing.setCreatedBy(updatedAssignment.getCreatedBy());
        // Base is NOT updated — prevents reassignment to another base
        return assignmentRepository.save(existing);
    }

    /** Delete an assignment. BASE_COMMANDER can only delete their own base's records. */
    public void deleteAssignment(Long id) {
        getAssignmentById(id); // enforces base access
        assignmentRepository.deleteById(id);
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

package com.kristalball.militaryasset.controller;

import com.kristalball.militaryasset.config.CurrentUserService;
import com.kristalball.militaryasset.service.DashboardService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * REST controller for the management dashboard.
 * Base URL: /api/dashboard
 *
 * RBAC enforcement:
 * - ADMIN            : may query any baseId.
 * - BASE_COMMANDER   : the baseId param is IGNORED — the server uses their own base
 *                      from the JWT token to prevent querying another base's data.
 * - LOGISTICS_OFFICER: may query any baseId (they manage logistics across bases).
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final CurrentUserService currentUserService;

    public DashboardController(DashboardService dashboardService,
                               CurrentUserService currentUserService) {
        this.dashboardService = dashboardService;
        this.currentUserService = currentUserService;
    }

    /**
     * GET /api/dashboard/metrics
     *
     * Query params: baseId, equipmentId, startDate (yyyy-MM-dd), endDate (yyyy-MM-dd)
     *
     * For BASE_COMMANDER: the baseId parameter is ignored; the server enforces
     * their own base from the JWT token.
     *
     * Returns: openingBalance, purchases, transferIn, transferOut,
     *          netMovement, assigned, expended, closingBalance
     */
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Integer>> getDashboardMetrics(
            @RequestParam(required = false) Long baseId,
            @RequestParam Long equipmentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        // For BASE_COMMANDER: ignore the supplied baseId and enforce their own base
        Long effectiveBaseId;
        if (currentUserService.isBaseCommander()) {
            effectiveBaseId = currentUserService.getCurrentUserBaseId();
        } else {
            // ADMIN and LOGISTICS_OFFICER may pass any baseId
            if (baseId == null) {
                return ResponseEntity.badRequest().build();
            }
            effectiveBaseId = baseId;
        }

        Map<String, Integer> metrics =
                dashboardService.calculateDashboardMetrics(
                        effectiveBaseId, equipmentId, startDate, endDate);

        return ResponseEntity.ok(metrics);
    }
}

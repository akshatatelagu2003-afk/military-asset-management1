package com.kristalball.militaryasset.service;

import com.kristalball.militaryasset.repository.AssignmentRepository;
import com.kristalball.militaryasset.repository.ExpenditureRepository;
import com.kristalball.militaryasset.repository.PurchaseRepository;
import com.kristalball.militaryasset.repository.TransferRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * DashboardService calculates the 8 key metrics for the asset management dashboard.
 *
 * All calculations are scoped to a specific base, a specific equipment type,
 * and a date range (startDate to endDate, inclusive).
 *
 * Approved Formulas:
 *   Net Movement     = Purchases + Transfer In - Transfer Out
 *   Closing Balance  = Opening Balance + Purchases + Transfer In - Transfer Out - Expended
 *
 * NOTE: "Assigned" is tracked separately and does NOT affect Closing Balance.
 */
@Service
public class DashboardService {

    private final PurchaseRepository purchaseRepository;
    private final TransferRepository transferRepository;
    private final AssignmentRepository assignmentRepository;
    private final ExpenditureRepository expenditureRepository;

    // Constructor injection
    public DashboardService(PurchaseRepository purchaseRepository,
                            TransferRepository transferRepository,
                            AssignmentRepository assignmentRepository,
                            ExpenditureRepository expenditureRepository) {
        this.purchaseRepository = purchaseRepository;
        this.transferRepository = transferRepository;
        this.assignmentRepository = assignmentRepository;
        this.expenditureRepository = expenditureRepository;
    }

    /**
     * Calculate all 8 dashboard metrics for a given base, equipment type, and date range.
     *
     * @param baseId       the ID of the military base to scope the dashboard to
     * @param equipmentId  the ID of the equipment type to report on
     * @param startDate    the first day of the reporting period (inclusive)
     * @param endDate      the last day of the reporting period (inclusive)
     * @return a map of metric names to their integer values
     */
    public Map<String, Integer> calculateDashboardMetrics(Long baseId,
                                                          Long equipmentId,
                                                          LocalDate startDate,
                                                          LocalDate endDate) {

        // ── STEP 1: Opening Balance ───────────────────────────────────────────
        // Opening Balance = everything that happened BEFORE startDate.
        // Formula: historicalPurchases + historicalTransferIn
        //          - historicalTransferOut - historicalExpended
        //
        // This represents how many units of this equipment the base already had
        // on the first day of the reporting period.

        int histPurchases   = purchaseRepository.sumQuantityByBaseAndEquipmentBefore(
                                    baseId, equipmentId, startDate);

        int histTransferIn  = transferRepository.sumTransferInByBaseAndEquipmentBefore(
                                    baseId, equipmentId, startDate);

        int histTransferOut = transferRepository.sumTransferOutByBaseAndEquipmentBefore(
                                    baseId, equipmentId, startDate);

        int histExpended    = expenditureRepository.sumQuantityByBaseAndEquipmentBefore(
                                    baseId, equipmentId, startDate);

        int openingBalance  = histPurchases + histTransferIn - histTransferOut - histExpended;


        // ── STEP 2: Period Metrics (startDate to endDate, inclusive) ──────────
        // These represent what happened DURING the selected reporting period.

        // Purchases: equipment bought and received at this base in this period
        int purchases  = purchaseRepository.sumQuantityByBaseAndEquipmentAndDateRange(
                                baseId, equipmentId, startDate, endDate);

        // Transfer In: equipment received from another base in this period
        int transferIn = transferRepository.sumTransferInByBaseAndEquipmentAndDateRange(
                                baseId, equipmentId, startDate, endDate);

        // Transfer Out: equipment sent to another base in this period
        int transferOut = transferRepository.sumTransferOutByBaseAndEquipmentAndDateRange(
                                baseId, equipmentId, startDate, endDate);

        // Expended: equipment consumed, destroyed, or written off in this period
        int expended   = expenditureRepository.sumQuantityByBaseAndEquipmentAndDateRange(
                                baseId, equipmentId, startDate, endDate);

        // Assigned: equipment issued to personnel in this period
        // Tracked separately — does NOT reduce the Closing Balance.
        int assigned   = assignmentRepository.sumQuantityByBaseAndEquipmentAndDateRange(
                                baseId, equipmentId, startDate, endDate);


        // ── STEP 3: Derived Metrics ───────────────────────────────────────────

        // Net Movement = Purchases + Transfer In - Transfer Out
        // (Expended is NOT part of Net Movement)
        int netMovement = purchases + transferIn - transferOut;

        // Closing Balance = Opening Balance + Net Movement - Expended
        // Expanded: Opening Balance + Purchases + Transfer In - Transfer Out - Expended
        // (Assigned is NOT subtracted)
        int closingBalance = openingBalance + purchases + transferIn - transferOut - expended;


        // ── STEP 4: Build and return the result map ───────────────────────────
        // LinkedHashMap preserves insertion order so the API response is readable.
        Map<String, Integer> metrics = new LinkedHashMap<>();
        metrics.put("openingBalance", openingBalance);
        metrics.put("purchases",      purchases);
        metrics.put("transferIn",     transferIn);
        metrics.put("transferOut",    transferOut);
        metrics.put("netMovement",    netMovement);
        metrics.put("assigned",       assigned);
        metrics.put("expended",       expended);
        metrics.put("closingBalance", closingBalance);

        return metrics;
    }
}

package com.kristalball.militaryasset.repository;

import com.kristalball.militaryasset.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    /**
     * Sum all quantities purchased at a given base for a given equipment
     * within a date range (inclusive). Used by DashboardService.
     */
    @Query("SELECT COALESCE(SUM(p.quantity), 0) FROM Purchase p " +
           "WHERE p.base.id = :baseId " +
           "AND p.equipment.id = :equipmentId " +
           "AND p.purchaseDate >= :startDate " +
           "AND p.purchaseDate <= :endDate")
    int sumQuantityByBaseAndEquipmentAndDateRange(
            @Param("baseId") Long baseId,
            @Param("equipmentId") Long equipmentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Sum all quantities purchased at a given base for a given equipment
     * BEFORE a given date. Used to calculate Opening Balance.
     */
    @Query("SELECT COALESCE(SUM(p.quantity), 0) FROM Purchase p " +
           "WHERE p.base.id = :baseId " +
           "AND p.equipment.id = :equipmentId " +
           "AND p.purchaseDate < :beforeDate")
    int sumQuantityByBaseAndEquipmentBefore(
            @Param("baseId") Long baseId,
            @Param("equipmentId") Long equipmentId,
            @Param("beforeDate") LocalDate beforeDate);
}

package com.kristalball.militaryasset.repository;

import com.kristalball.militaryasset.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, Long> {

    /**
     * Sum quantities transferred INTO a given base for a given equipment
     * within a date range. Used by DashboardService for "Transfer In".
     */
    @Query("SELECT COALESCE(SUM(t.quantity), 0) FROM Transfer t " +
           "WHERE t.toBase.id = :baseId " +
           "AND t.equipment.id = :equipmentId " +
           "AND t.transferDate >= :startDate " +
           "AND t.transferDate <= :endDate")
    int sumTransferInByBaseAndEquipmentAndDateRange(
            @Param("baseId") Long baseId,
            @Param("equipmentId") Long equipmentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Sum quantities transferred OUT of a given base for a given equipment
     * within a date range. Used by DashboardService for "Transfer Out".
     */
    @Query("SELECT COALESCE(SUM(t.quantity), 0) FROM Transfer t " +
           "WHERE t.fromBase.id = :baseId " +
           "AND t.equipment.id = :equipmentId " +
           "AND t.transferDate >= :startDate " +
           "AND t.transferDate <= :endDate")
    int sumTransferOutByBaseAndEquipmentAndDateRange(
            @Param("baseId") Long baseId,
            @Param("equipmentId") Long equipmentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Sum Transfer In BEFORE a given date. Used for Opening Balance.
     */
    @Query("SELECT COALESCE(SUM(t.quantity), 0) FROM Transfer t " +
           "WHERE t.toBase.id = :baseId " +
           "AND t.equipment.id = :equipmentId " +
           "AND t.transferDate < :beforeDate")
    int sumTransferInByBaseAndEquipmentBefore(
            @Param("baseId") Long baseId,
            @Param("equipmentId") Long equipmentId,
            @Param("beforeDate") LocalDate beforeDate);

    /**
     * Sum Transfer Out BEFORE a given date. Used for Opening Balance.
     */
    @Query("SELECT COALESCE(SUM(t.quantity), 0) FROM Transfer t " +
           "WHERE t.fromBase.id = :baseId " +
           "AND t.equipment.id = :equipmentId " +
           "AND t.transferDate < :beforeDate")
    int sumTransferOutByBaseAndEquipmentBefore(
            @Param("baseId") Long baseId,
            @Param("equipmentId") Long equipmentId,
            @Param("beforeDate") LocalDate beforeDate);
}

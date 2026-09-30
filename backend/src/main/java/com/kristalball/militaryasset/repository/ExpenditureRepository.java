package com.kristalball.militaryasset.repository;

import com.kristalball.militaryasset.entity.Expenditure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface ExpenditureRepository extends JpaRepository<Expenditure, Long> {

    /**
     * Sum all quantities expended at a given base for a given equipment
     * within a date range. Used by DashboardService for "Expended".
     */
    @Query("SELECT COALESCE(SUM(e.quantity), 0) FROM Expenditure e " +
           "WHERE e.base.id = :baseId " +
           "AND e.equipment.id = :equipmentId " +
           "AND e.expendedDate >= :startDate " +
           "AND e.expendedDate <= :endDate")
    int sumQuantityByBaseAndEquipmentAndDateRange(
            @Param("baseId") Long baseId,
            @Param("equipmentId") Long equipmentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Sum expenditures BEFORE a given date. Used for Opening Balance.
     */
    @Query("SELECT COALESCE(SUM(e.quantity), 0) FROM Expenditure e " +
           "WHERE e.base.id = :baseId " +
           "AND e.equipment.id = :equipmentId " +
           "AND e.expendedDate < :beforeDate")
    int sumQuantityByBaseAndEquipmentBefore(
            @Param("baseId") Long baseId,
            @Param("equipmentId") Long equipmentId,
            @Param("beforeDate") LocalDate beforeDate);
}

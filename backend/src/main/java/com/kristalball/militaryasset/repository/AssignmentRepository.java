package com.kristalball.militaryasset.repository;

import com.kristalball.militaryasset.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    /**
     * Sum all quantities assigned at a given base for a given equipment
     * within a date range. Used by DashboardService for "Assigned".
     */
    @Query("SELECT COALESCE(SUM(a.quantity), 0) FROM Assignment a " +
           "WHERE a.base.id = :baseId " +
           "AND a.equipment.id = :equipmentId " +
           "AND a.assignedDate >= :startDate " +
           "AND a.assignedDate <= :endDate")
    int sumQuantityByBaseAndEquipmentAndDateRange(
            @Param("baseId") Long baseId,
            @Param("equipmentId") Long equipmentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}

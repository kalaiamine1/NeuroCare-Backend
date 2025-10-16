package com.BrainStack.Repository;

import com.BrainStack.Entity.Appointment;
import com.BrainStack.Enums.AppointmentStatus;
import com.BrainStack.Enums.AppointmentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /**
     * Trouve tous les rendez-vous d'un parent
     */
    Page<Appointment> findByParentId(Long parentId, Pageable pageable);

    /**
     * Trouve tous les rendez-vous d'un professionnel
     */
    Page<Appointment> findByProfessionalId(Long professionalId, Pageable pageable);

    /**
     * Trouve tous les rendez-vous d'un enfant
     */
    Page<Appointment> findByChildId(Long childId, Pageable pageable);

    /**
     * Trouve les rendez-vous par type
     */
    Page<Appointment> findByType(AppointmentType type, Pageable pageable);

    /**
     * Trouve les rendez-vous par statut
     */
    Page<Appointment> findByStatus(AppointmentStatus status, Pageable pageable);

    /**
     * Trouve les rendez-vous dans une plage horaire
     */
    @Query("SELECT a FROM Appointment a WHERE a.startTime >= :startTime AND a.endTime <= :endTime")
    List<Appointment> findAppointmentsBetween(@Param("startTime") LocalDateTime startTime,
                                              @Param("endTime") LocalDateTime endTime);

    /**
     * Trouve les conflits d'horaires pour un professionnel
     */
    @Query("SELECT a FROM Appointment a WHERE a.professionalId = :professionalId " +
            "AND a.status != 'CANCELLED' " +
            "AND ((a.startTime < :endTime AND a.endTime > :startTime))")
    List<Appointment> findConflicts(@Param("professionalId") Long professionalId,
                                    @Param("startTime") LocalDateTime startTime,
                                    @Param("endTime") LocalDateTime endTime);

    /**
     * Trouve les conflits d'horaires pour un parent (même parent, horaires qui se chevauchent)
     */
    @Query("SELECT a FROM Appointment a WHERE a.parentId = :parentId " +
            "AND a.status != 'CANCELLED' " +
            "AND ((a.startTime < :endTime AND a.endTime > :startTime))")
    List<Appointment> findParentConflicts(@Param("parentId") Long parentId,
                                          @Param("startTime") LocalDateTime startTime,
                                          @Param("endTime") LocalDateTime endTime);

    /**
     * Trouve les rendez-vous à venir
     */
    @Query("SELECT a FROM Appointment a WHERE a.startTime > :now AND a.status != 'CANCELLED' ORDER BY a.startTime ASC")
    List<Appointment> findUpcomingAppointments(@Param("now") LocalDateTime now);

    /**
     * Trouve les rendez-vous passés
     */
    @Query("SELECT a FROM Appointment a WHERE a.endTime < :now AND a.status != 'CANCELLED' ORDER BY a.startTime DESC")
    List<Appointment> findPastAppointments(@Param("now") LocalDateTime now);

    /**
     * Compte les rendez-vous non notifiés
     */
    Long countByNotificationSentFalseAndStartTimeGreaterThan(LocalDateTime startTime);
}
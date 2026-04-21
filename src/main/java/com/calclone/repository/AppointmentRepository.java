package com.calclone.repository;

import com.calclone.entity.Appointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    boolean existsByEventTypeIdAndDateAndStartTime(Long eventTypeId, LocalDate date, String startTime);
    List<Appointment> findByEventTypeUserUsernameOrderByDateDescStartTimeDesc(String username);

    Page<Appointment> findByStatus(Appointment.BookingStatus status, Pageable pageable);

    Page<Appointment> findByDateAfter(LocalDate date, Pageable pageable);

    Page<Appointment> findByDateBefore(LocalDate date, Pageable pageable);

    Page<Appointment> findByStatusAndDateAfter(Appointment.BookingStatus status, LocalDate date, Pageable pageable);

    Page<Appointment> findByStatusAndDateBefore(Appointment.BookingStatus status, LocalDate date, Pageable pageable);
}

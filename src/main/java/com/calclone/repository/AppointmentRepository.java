package com.calclone.repository;

import com.calclone.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    boolean existsByEventTypeIdAndDateAndStartTime(Long eventTypeId, LocalDate date, String startTime);
    List<Appointment> findByEventTypeUserUsernameOrderByDateDescStartTimeDesc(String username);
}

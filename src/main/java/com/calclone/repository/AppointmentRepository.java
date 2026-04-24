package com.calclone.repository;

import com.calclone.entity.Appointment;
import com.calclone.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    Page<Appointment> findByStatus(Appointment.BookingStatus status, Pageable pageable);

    List<Appointment> findByDateAndStartTimeAndEventType_User(LocalDate date, String startTime, User user);

    Page<Appointment> findByStatusInAndDateAfter(
            List<Appointment.BookingStatus> statuses, LocalDate date, Pageable pageable);

    Page<Appointment> findByStatusAndEventType_IdIn(
            Appointment.BookingStatus status,
            List<Long> eventTypeIds,
            Pageable pageable);

    Page<Appointment> findByStatusInAndDateAfterAndEventType_IdIn(
            List<Appointment.BookingStatus> statuses,
            LocalDate date,
            List<Long> eventTypeIds,
            Pageable pageable);
}

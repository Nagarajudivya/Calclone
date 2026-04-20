package com.calclone.repository;

import com.calclone.entity.Availability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AvailabilityRepository extends JpaRepository<Availability, Long> {
    Optional<Availability> findByDayOfWeek(String dayOfWeek);
    Optional<Availability> findByDayOfWeekAndScheduleId(String day, Long scheduleId);

    List<Availability> findByScheduleId(Long scheduleId);
}

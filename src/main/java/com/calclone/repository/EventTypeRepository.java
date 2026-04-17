package com.calclone.repository;

import com.calclone.entity.EventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventTypeRepository extends JpaRepository<EventType, Long> {
    List<EventType> findByUserId(Long userId);
}

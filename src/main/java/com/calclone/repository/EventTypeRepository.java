package com.calclone.repository;

import com.calclone.entity.EventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventTypeRepository extends JpaRepository<EventType, Long> {
    List<EventType> findByUserId(Long userId);

    Optional<EventType> findByUserUsernameAndSlug(String username, String slug);
    boolean existsByUserIdAndSlug(Long userId, String slug);

    List<EventType> findByTitleContainingIgnoreCase(String keyword);
}

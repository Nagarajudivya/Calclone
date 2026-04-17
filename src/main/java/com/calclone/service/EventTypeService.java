package com.calclone.service;

import com.calclone.entity.EventType;
import com.calclone.repository.EventTypeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventTypeService {

    private final EventTypeRepository repository;

    public EventTypeService(EventTypeRepository repository) {
        this.repository = repository;
    }

    public EventType save(EventType eventType) {
        return repository.save(eventType);
    }

    public List<EventType> getByUser(Long userId) {
        return repository.findByUserId(userId);
    }

    public EventType getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public EventType findByUsernameAndSlug(String username, String slug) {
        return repository.findByUserUsernameAndSlug(username, slug)
                .orElseThrow(() -> new RuntimeException("Event not found for user: " + username + " with slug: " + slug));
    }
}




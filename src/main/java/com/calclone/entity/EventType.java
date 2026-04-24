package com.calclone.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "event_type", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "slug"})
})
public class EventType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String slug;

    private String description;

    private int duration;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private boolean active = true;

    private String location = "Cal Video (Default)";

    @Column(name = "schedule_id")
    private Long scheduleId;
}





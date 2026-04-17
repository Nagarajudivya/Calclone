package com.calclone.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class EventType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(unique = true)
    private String slug;

    private String description;

    private int duration;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private boolean active = true;
}





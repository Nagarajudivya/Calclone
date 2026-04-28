package com.calclone.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Availability {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String dayOfWeek;

    private LocalTime startTime;

    private LocalTime endTime;

    private boolean active;

    @ManyToOne
    @JsonIgnore
    private Schedule schedule;

    @OneToMany(mappedBy = "availability", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TimeSlot> slots = new ArrayList<>();


    public Availability() {}

    public Availability(String dayOfWeek, LocalTime startTime, LocalTime endTime, boolean active) {
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.active = active;
    }
}
package com.calclone.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
@Table(name = "appointmet")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String guestName;

    private String guestEmail;

    private String startTime;

    private LocalDate date;

    private String notes;

    @ManyToOne
    private EventType eventType;


    public enum BookingStatus {
        UPCOMING,
        PAST,
        CANCELLED
    }

    @Enumerated(EnumType.STRING)
    private BookingStatus status;
}
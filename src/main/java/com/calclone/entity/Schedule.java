package com.calclone.entity;

import jakarta.persistence.*;
        import lombok.Data;
import java.util.List;

@Entity
@Data
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String activeDays;
    private String timeRange;
    private boolean isDefault;
    private Long userId;


    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL)
    private List<Availability> availabilities;
}
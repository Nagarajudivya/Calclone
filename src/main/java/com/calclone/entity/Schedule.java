package com.calclone.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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


    @JsonIgnore
    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL)
    private List<Availability> availabilities;


    public Map<String, String> getSlots() {
        Map<String, String> slots = new LinkedHashMap<>();
        String range = (this.timeRange != null) ? this.timeRange : "9:00 AM - 5:00 PM";
        String activeDaysStr = (this.activeDays != null) ? this.activeDays.trim() : "";

        String[] dayKeys = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        List<String> dayList = Arrays.asList(dayKeys);

        int startIdx = 0;
        int endIdx = dayList.size() - 1;

        if (activeDaysStr.contains(" - ")) {
            String[] parts = activeDaysStr.split(" - ");
            String startDay = parts[0].trim();
            String endDay   = parts[1].trim();

            for (int i = 0; i < dayKeys.length; i++) {
                if (dayKeys[i].equalsIgnoreCase(startDay)) startIdx = i;
                if (dayKeys[i].equalsIgnoreCase(endDay))   endIdx   = i;
            }
        }

        for (int i = 0; i < dayKeys.length; i++) {
            boolean isActive = (i >= startIdx && i <= endIdx);
            slots.put(dayKeys[i], isActive ? range : "Unavailable");
        }
        return slots;
    }
}
package com.calclone.controller;

import com.calclone.entity.Availability;
import com.calclone.entity.Schedule;
import com.calclone.entity.TimeSlot;
import com.calclone.repository.AvailabilityRepository;
import com.calclone.repository.ScheduleRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

        import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/availability")
public class AvailabilityController {

    private final AvailabilityRepository availabilityRepository;
    private final ScheduleRepository scheduleRepository;

    public AvailabilityController(AvailabilityRepository availabilityRepository, ScheduleRepository scheduleRepository){
        this.availabilityRepository = availabilityRepository;
        this.scheduleRepository = scheduleRepository;
    }

    private final List<String> days = Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");


    @PostMapping("/save")
    public String saveAvailability(jakarta.servlet.http.HttpServletRequest request) {

        for (String day : days) {
            Availability availability = availabilityRepository.findByDayOfWeek(day)
                    .orElse(new Availability());
            availability.setDayOfWeek(day);

            String activeStatus = request.getParameter(day + "_active");
            boolean isActive = (activeStatus != null);
            availability.setActive(isActive);

            availability.getSlots().clear();

            if (isActive) {
                String[] starts = request.getParameterValues(day + "_start[]");
                String[] ends = request.getParameterValues(day + "_end[]");

                if (starts != null && ends != null) {
                    for (int i = 0; i < starts.length; i++) {
                        if (!starts[i].isEmpty() && !ends[i].isEmpty()) {
                            TimeSlot slot = new TimeSlot();
                            slot.setStartTime(LocalTime.parse(starts[i]));
                            slot.setEndTime(LocalTime.parse(ends[i]));
                            slot.setAvailability(availability);
                            availability.getSlots().add(slot);
                        }
                    }
                }
            }
            availabilityRepository.save(availability);
        }
        return "redirect:/availability?success";
    }

    @GetMapping
    public String showAvailability(Model model) {
        Map<String, Object> currentUser = Map.of(
                "id", 1L,
                "username", "divya",
                "timezone", "Asia/Kolkata"
        );
        model.addAttribute("user", currentUser);

        List<Schedule> schedules = scheduleRepository.findByUserId(1L);

        if (schedules.isEmpty()) {
            Schedule defaultSchedule = new Schedule();
            defaultSchedule.setName("Working hours");
            defaultSchedule.setUserId(1L);
            defaultSchedule.setActiveDays("Mon - Fri");
            defaultSchedule.setTimeRange("9:00 AM - 5:00 PM");
            defaultSchedule.setDefault(true);

            scheduleRepository.save(defaultSchedule);
            schedules = List.of(defaultSchedule);
        }

        model.addAttribute("schedules", schedules);
        return "availability";
    }


    @GetMapping("/edit/{id}")
    public String editSchedule(@PathVariable Long id, Model model) {
        Long currentUserId = 1L;

        Schedule schedule = scheduleRepository.findById(id)
                .orElseGet(() -> scheduleRepository.findFirstByUserId(currentUserId)
                        .orElseThrow(() -> new RuntimeException("No schedules found for user " + currentUserId)));

        model.addAttribute("schedule", schedule);

        List<Availability> savedSettings = availabilityRepository.findAll();
        List<Availability> dailySettings = new java.util.ArrayList<>();

        for (String dayName : days) {
            Availability dayData = savedSettings.stream()
                    .filter(a -> a.getDayOfWeek().equalsIgnoreCase(dayName))
                    .findFirst()
                    .orElse(null);

            if (dayData == null) {

                dayData = new Availability();
                dayData.setDayOfWeek(dayName);
                dayData.setStartTime(LocalTime.of(9, 0));
                dayData.setEndTime(LocalTime.of(17, 0));
                dayData.setActive(!dayName.equalsIgnoreCase("Saturday") && !dayName.equalsIgnoreCase("Sunday"));
            }
            dailySettings.add(dayData);
        }

        model.addAttribute("dailySettings", dailySettings);
        model.addAttribute("timezone", "Asia/Kolkata");

        return "edit-availability";
    }


    @PostMapping("/schedule/create/{userId}")
    public String createSchedule(@PathVariable Long userId,
                                 @RequestParam("scheduleName") String name) {

        Schedule schedule = new Schedule();
        schedule.setName(name);
        schedule.setDefault(false);

        scheduleRepository.save(schedule);

        return "redirect:/availability";
    }
}
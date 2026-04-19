package com.calclone.controller;

import com.calclone.entity.Appointment;
import com.calclone.entity.EventType;
import com.calclone.repository.AppointmentRepository;
import com.calclone.service.EventTypeService;
import com.calclone.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
public class PublicBookingController {

    private final EventTypeService eventTypeService;
    private final AppointmentRepository appointmentRepository;
    private final UserService userService;

    public PublicBookingController(EventTypeService eventTypeService, AppointmentRepository appointmentRepository,
                                   UserService userService){
        this.eventTypeService = eventTypeService;
        this.appointmentRepository = appointmentRepository;
        this.userService = userService;
    }

//    @GetMapping("/book/{username}/{slug}")
//    public String showPublicCalendar(@PathVariable String username, @PathVariable String slug, Model model) {
//        EventType event = eventTypeService.findByUsernameAndSlug(username, slug);
//
//        if (event == null || !event.isActive()) {
//            return "error";
//        }
//
//        model.addAttribute("event", event);
//        model.addAttribute("user", event.getUser());
//        return "public-booking";
//    }

    @GetMapping("/book/{username}/{slug}")
    public String showPublicCalendar(@PathVariable String username,
                                     @PathVariable String slug,
                                     @RequestParam(required = false) Long guestId, // 2. Optional param for testing
                                     Model model) {

        EventType event = eventTypeService.findByUsernameAndSlug(username, slug);
        if (event == null || !event.isActive()) {
            return "error";
        }

        model.addAttribute("event", event);
        model.addAttribute("user", event.getUser()); // This is the HOST

        // 3. DYNAMIC AUTOFILL: Fetch a user from DB to act as the Guest
        // If guestId is passed in URL (?guestId=1), we fetch that user.
        if (guestId != null) {
            userService.getById(guestId).ifPresent(guest -> {
                model.addAttribute("loggedInUser", guest);
            });
        }

        return "public-booking";
    }

    @PostMapping("/book/confirm")
    public String confirmBooking(@RequestParam Long eventTypeId,
                                 @RequestParam String guestName,
                                 @RequestParam String guestEmail,
                                 @RequestParam String startTime,
                                 @RequestParam String date,
                                 @RequestParam(required = false) String notes) {

        Appointment appt = new Appointment();
        appt.setGuestName(guestName);
        appt.setGuestEmail(guestEmail);
        appt.setStartTime(startTime);
        appt.setDate(LocalDate.parse(date));
        appt.setNotes(notes);
        appt.setEventType(eventTypeService.getById(eventTypeId));

        appointmentRepository.save(appt);

        return "redirect:/booking-success";
    }

    @GetMapping("/booking-success")
    @ResponseBody
    public String success() {
        return "<h1>Booking Confirmed!</h1><p>Check your email for details.</p>";
    }
}

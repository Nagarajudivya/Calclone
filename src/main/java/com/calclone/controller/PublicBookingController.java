package com.calclone.controller;

import com.calclone.entity.Appointment;
import com.calclone.entity.EventType;
import com.calclone.repository.AppointmentRepository;
import com.calclone.repository.UserRepository;
import com.calclone.service.EventTypeService;
import com.calclone.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@Controller
public class PublicBookingController {

    private final EventTypeService eventTypeService;
    private final AppointmentRepository appointmentRepository;
    private final UserService userService;
    private final UserRepository userRepository;

    public PublicBookingController(EventTypeService eventTypeService, AppointmentRepository appointmentRepository,
                                   UserService userService,  UserRepository userRepository){
        this.eventTypeService = eventTypeService;
        this.appointmentRepository = appointmentRepository;
        this.userService = userService;
        this.userRepository = userRepository;
    }


    @GetMapping("/book/{username}/{slug}")
    public String showPublicCalendar(@PathVariable String username,
                                     @PathVariable String slug,
                                     @RequestParam(required = false) Long guestId,
                                     Model model) {

        EventType event = eventTypeService.findByUsernameAndSlug(username, slug);
        if (event == null || !event.isActive()) {
            return "error";
        }

        model.addAttribute("event", event);
        model.addAttribute("user", event.getUser());


        if (guestId != null) {
            userService.getById(guestId).ifPresent(guest -> {
                model.addAttribute("loggedInUser", guest);
            });
        }


        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()
                && !(auth.getPrincipal() instanceof String)) {
            userRepository.findByEmail(auth.getName()).ifPresent(loggedInUser -> {
                model.addAttribute("loggedInName", loggedInUser.getUsername());
                model.addAttribute("loggedInEmail", loggedInUser.getEmail());
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
                                 @RequestParam(required = false) String notes,
                                 Model model) {

        LocalDate bookingDate = LocalDate.parse(date);


        boolean conflict = appointmentRepository.existsByEventTypeIdAndDateAndStartTime(eventTypeId, bookingDate, startTime);

        if (conflict) {
            // Prepare data for Error View (Image 2)
            model.addAttribute("errorTitle", "Could not book the meeting.");
            model.addAttribute("errorMessage", "Attempting to book a meeting that is already taken or in the past.");
            model.addAttribute("event", eventTypeService.getById(eventTypeId));
            return "booking-error";
        }

        Appointment appt = new Appointment();
        appt.setGuestName(guestName);
        appt.setGuestEmail(guestEmail);
        appt.setStartTime(startTime);
        appt.setDate(bookingDate);
        appt.setNotes(notes);
        appt.setEventType(eventTypeService.getById(eventTypeId));

        Appointment savedAppt = appointmentRepository.save(appt);


        return "redirect:/booking-success/" + savedAppt.getId();
    }

    @GetMapping("/booking-success/{id}")
    public String showSuccess(@PathVariable Long id, Model model) {
        Appointment appt = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        model.addAttribute("appt", appt);
        return "booking-success";
    }

    @PostMapping("/book/cancel/{id}")
    public String cancelBooking(@PathVariable Long id, @RequestParam String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            return "redirect:/booking-success/" + id + "?error=Reason+Required";
        }
        appointmentRepository.deleteById(id);
        return "redirect:/bookings?cancelled=true";
    }

    @GetMapping("/bookings")
    public String showBookingsDashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        userRepository.findByEmail(email).ifPresent(user -> {
            model.addAttribute("user", user);
        });

        List<Appointment> allBookings = appointmentRepository.findAll();
        model.addAttribute("bookings", allBookings);
        return "bookings";
    }
}

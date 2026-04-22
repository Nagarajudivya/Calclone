package com.calclone.controller;

import com.calclone.entity.Appointment;
import com.calclone.entity.EventType;
import com.calclone.entity.User;
import com.calclone.repository.AppointmentRepository;
import com.calclone.repository.UserRepository;
import com.calclone.service.EventTypeService;
import com.calclone.service.GoogleMeetService;
import com.calclone.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
public class PublicBookingController {

    private final EventTypeService eventTypeService;
    private final AppointmentRepository appointmentRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final GoogleMeetService googleMeetService;

    public PublicBookingController(EventTypeService eventTypeService, AppointmentRepository appointmentRepository,
                                   UserService userService,  UserRepository userRepository, GoogleMeetService googleMeetService){
        this.eventTypeService = eventTypeService;
        this.appointmentRepository = appointmentRepository;
        this.userService = userService;
        this.userRepository = userRepository;
        this.googleMeetService = googleMeetService;
    }


    @GetMapping("/book/{username}/{slug}")
    public String showPublicCalendar(@PathVariable String username,
                                     @PathVariable String slug,
                                     @RequestParam(required = false) Long rescheduleId,
                                     HttpServletRequest request,
                                     Model model) {
        EventType event = eventTypeService.findByUsernameAndSlug(username, slug);
        if (event == null || !event.isActive()) return "error";

        model.addAttribute("event", event);
        model.addAttribute("user", event.getUser());


        if (rescheduleId != null) {
            appointmentRepository.findById(rescheduleId).ifPresent(oldAppt -> {
                model.addAttribute("rescheduleId", rescheduleId);
                model.addAttribute("oldAppt", oldAppt);
            });
        }
        return "public-booking";
    }

    @PostMapping("/book/confirm")
    public String confirmBooking(@RequestParam Long eventTypeId,
                                 @RequestParam(required = false) Long rescheduleId,
                                 @RequestParam String guestName,
                                 @RequestParam String guestEmail,
                                 @RequestParam String startTime,
                                 @RequestParam String date,
                                 @RequestParam(required = false) String notes,
                                 HttpServletRequest request,
                                 Model model) {

        LocalDate bookingDate = LocalDate.parse(date);

        EventType eventType = eventTypeService.getById(eventTypeId);
        User host = eventType.getUser();

        System.out.println("=== BOOKING DEBUG ===");
        System.out.println("Host: " + host.getEmail());
        System.out.println("Host token in DB: " + host.getGoogleAccessToken());

        String token = host.getGoogleAccessToken();
        if (token == null || token.isEmpty()) {
            token = (String) request.getSession().getAttribute("googleAccessToken");
            System.out.println("Using session token as fallback: " + (token != null ? "found" : "NOT FOUND"));
        }

        String meetLink = null;
        if (token != null && !token.isEmpty()) {
            meetLink = googleMeetService.createGoogleMeet(token);
            System.out.println("Generated Meet Link: " + meetLink);
        } else {
            System.out.println("No token available!");
        }

        Appointment appt;
        if (rescheduleId != null) {
            appt = appointmentRepository.findById(rescheduleId).orElse(new Appointment());
        } else {
            appt = new Appointment();
        }

        appt.setGuestName(guestName);
        appt.setGuestEmail(guestEmail);
        appt.setStartTime(startTime);
        appt.setDate(bookingDate);
        appt.setNotes(notes);
        appt.setEventType(eventTypeService.getById(eventTypeId));
        appt.setMeetingLink(meetLink);

        Appointment savedAppt = appointmentRepository.save(appt);
        return "redirect:/booking-success/" + savedAppt.getId() + (rescheduleId != null ? "?rescheduled=true" : "");
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

        Appointment appt = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));

        appt.setStatus(Appointment.BookingStatus.CANCELLED);
        appointmentRepository.save(appt);

        return "redirect:/bookings?status=cancelled";
    }


    @GetMapping("/bookings")
    public String showBookingsDashboard(
            @RequestParam(defaultValue = "upcoming") String filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        List<Appointment> all = appointmentRepository.findAll();

        for (Appointment appt : all) {

            if (appt.getStatus() == null) {

                if (appt.getDate().isBefore(LocalDate.now())) {
                    appt.setStatus(Appointment.BookingStatus.PAST);
                } else {
                    appt.setStatus(Appointment.BookingStatus.UPCOMING);
                }
            }
        }

        appointmentRepository.saveAll(all);

        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
        Page<Appointment> bookingPage;

        LocalDate today = LocalDate.now();

        switch (filter.toLowerCase()) {

            case "past":
                bookingPage = appointmentRepository
                        .findByStatusAndDateBefore(Appointment.BookingStatus.PAST, today, pageable);
                break;

            case "cancelled":
                bookingPage = appointmentRepository
                        .findByStatus(Appointment.BookingStatus.CANCELLED, pageable);
                break;

            case "upcoming":
            default:
                bookingPage = appointmentRepository
                        .findByStatusAndDateAfter(Appointment.BookingStatus.UPCOMING, today.minusDays(1), pageable);
                break;
        }

        model.addAttribute("bookings", bookingPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", bookingPage.getTotalPages());
        model.addAttribute("totalItems", bookingPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("filter", filter);

        return "bookings";
    }


    @PostMapping("/book")
    public String createBooking(
            @RequestParam String date,
            @RequestParam String startTime,
            @RequestParam String guestName,
            HttpServletRequest request
    ) {

        String token = (String) request.getSession().getAttribute("googleAccessToken");

        String meetLink = googleMeetService.createGoogleMeet(token);

        System.out.println("Meet Link: " + meetLink);

        Appointment appt = new Appointment();
        appt.setDate(LocalDate.parse(date));
        appt.setStartTime(startTime);
        appt.setGuestName(guestName);
        appt.setMeetingLink(meetLink);

        appointmentRepository.save(appt);

        return "redirect:/bookings";
    }


    @GetMapping("/admin/backfill-meet-links")
    @ResponseBody
    public String backfillMeetLinks(HttpServletRequest request) {
        String token = (String) request.getSession().getAttribute("googleAccessToken");
        if (token == null) return "No token in session. Please login first.";

        List<Appointment> all = appointmentRepository.findAll();
        int updated = 0;

        for (Appointment appt : all) {
            if (appt.getMeetingLink() == null || appt.getMeetingLink().isEmpty()) {
                String link = googleMeetService.createGoogleMeet(token);
                if (link != null) {
                    appt.setMeetingLink(link);
                    appointmentRepository.save(appt);
                    updated++;
                }
            }
        }
        return "Updated " + updated + " appointments with meet links.";
    }
}

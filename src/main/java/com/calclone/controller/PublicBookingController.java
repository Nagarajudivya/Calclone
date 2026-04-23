package com.calclone.controller;

import com.calclone.entity.Appointment;
import com.calclone.entity.EventType;
import com.calclone.entity.User;
import com.calclone.repository.AppointmentRepository;
import com.calclone.repository.UserRepository;
import com.calclone.service.EmailService;
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
import java.util.ArrayList;
import java.util.List;

@Controller
public class PublicBookingController {

    private final EventTypeService eventTypeService;
    private final AppointmentRepository appointmentRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final GoogleMeetService googleMeetService;
    private final EmailService emailService;

    public PublicBookingController(EventTypeService eventTypeService, AppointmentRepository appointmentRepository,
                                   UserService userService,  UserRepository userRepository,
                                   GoogleMeetService googleMeetService, EmailService emailService){
        this.eventTypeService = eventTypeService;
        this.appointmentRepository = appointmentRepository;
        this.userService = userService;
        this.userRepository = userRepository;
        this.googleMeetService = googleMeetService;
        this.emailService = emailService;
    }


    @GetMapping("/book/{username}/{slug}")
    public String showPublicCalendar(@PathVariable String username,
                                     @PathVariable String slug,
                                     @RequestParam(required = false) Long rescheduleId,
                                     @RequestParam(required = false) String date,
                                     Model model) {
        EventType event = eventTypeService.findByUsernameAndSlug(username, slug);
        if (event == null || !event.isActive()) return "error";

        User host = event.getUser();

        model.addAttribute("event", event);
        model.addAttribute("user", host);
//        model.addAttribute("user", event.getUser());


        if (date != null) {

            String token = host.getGoogleAccessToken();

            List<String[]> busySlots = googleMeetService.getBusySlots(token, date);
            System.out.println("Total busy slots found: " + busySlots.size());

            List<String> slots = generateSlots(event.getDuration(), busySlots);
            System.out.println("Available slots: " + slots); // ADD THIS
            model.addAttribute("slots", slots);

        }

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

        boolean alreadyBooked = appointmentRepository
                .findByDateAndStartTimeAndEventType_User(bookingDate, startTime, host)
                .stream()
                .anyMatch(a -> a.getStatus() != Appointment.BookingStatus.CANCELLED
                        && (rescheduleId == null || !a.getId().equals(rescheduleId)));

        if (alreadyBooked) {
            model.addAttribute("error", "This slot is already booked. Please choose another time.");
            model.addAttribute("event", eventType);
            model.addAttribute("user", host);

            return "redirect:/booking-error?eventTypeId=" + eventTypeId +
                    "&message=This+slot+is+already+booked.+Please+choose+another+time.";
        }

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

            java.time.format.DateTimeFormatter formatter =
                    java.time.format.DateTimeFormatter.ofPattern("h:mma");

            java.time.LocalTime start = java.time.LocalTime.parse(startTime.toUpperCase(), formatter);


            java.time.LocalTime end = start.plusMinutes(eventType.getDuration());


            String startDateTime = date + "T" + start + ":00+05:30";
            String endDateTime = date + "T" + end + ":00+05:30";


            meetLink = googleMeetService.createGoogleMeet(
                    token,
                    startDateTime,
                    endDateTime,
                    guestEmail
            );

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
        emailService.sendBookingConfirmation(savedAppt);
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


        User user = userService.getLoggedInUser();
        model.addAttribute("user", user);

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


    private List<String> generateSlots(int duration, List<String[]> busySlots) {

        List<String> slots = new ArrayList<>();

        java.time.LocalTime dayStart = java.time.LocalTime.of(9, 0);
        java.time.LocalTime dayEnd   = java.time.LocalTime.of(17, 0);

        java.time.LocalTime current = dayStart;

        while (!current.plusMinutes(duration).isAfter(dayEnd)) {

            java.time.LocalTime slotStart = current;
            java.time.LocalTime slotEnd   = current.plusMinutes(duration);


            boolean isBusy = busySlots.stream().anyMatch(b -> {
                try {
                    // Google returns ISO strings like "2025-04-24T17:00:00+05:30"
                    java.time.LocalTime busyStart = java.time.OffsetDateTime.parse(b[0]).toLocalTime();
                    java.time.LocalTime busyEnd   = java.time.OffsetDateTime.parse(b[1]).toLocalTime();


                    return slotStart.isBefore(busyEnd) && slotEnd.isAfter(busyStart);
                } catch (Exception e) {
                    return false;
                }
            });

            if (!isBusy) {
                slots.add(current.format(java.time.format.DateTimeFormatter.ofPattern("h:mma")));
            }

            current = current.plusMinutes(duration);
        }

        return slots;
    }

    @GetMapping("/booking-error")
    public String showBookingError(
            @RequestParam String message,
            @RequestParam Long eventTypeId,
            Model model) {
        EventType eventType = eventTypeService.getById(eventTypeId);
        model.addAttribute("event", eventType);
        model.addAttribute("errorTitle", "Slot Already Booked");
        model.addAttribute("errorMessage", message);
        return "booking-error";
    }
}

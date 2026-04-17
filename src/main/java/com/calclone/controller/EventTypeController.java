package com.calclone.controller;

import com.calclone.entity.EventType;
import com.calclone.entity.User;
import com.calclone.service.EventTypeService;
import com.calclone.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/events")
public class EventTypeController {

    private final EventTypeService eventTypeService;
    private final UserService userService;

    public EventTypeController(EventTypeService eventTypeService, UserService userService){
        this.eventTypeService = eventTypeService;
        this.userService = userService;
    }

    @GetMapping("/create/{userId}")
    public String createForm(@PathVariable Long userId, Model model) {
        model.addAttribute("event", new EventType());
        model.addAttribute("userId", userId);
        return "create-event";
    }

    @PostMapping("/create/{userId}")
    public String saveEvent(@PathVariable Long userId,
                            @ModelAttribute EventType eventType) {

        User user = userService.getById(userId);
        eventType.setUser(user);

        eventTypeService.save(eventType);

        return "redirect:/events/" + userId;
    }

    @GetMapping("/{userId}")
    public String listEvents(@PathVariable Long userId, Model model) {
        User user = userService.getById(userId);
        model.addAttribute("events", eventTypeService.getByUser(userId));
        model.addAttribute("user", user);
        model.addAttribute("userId", userId);
        return "events";
    }


    @GetMapping("/delete/{id}/{userId}")
    public String delete(@PathVariable Long id, @PathVariable Long userId) {
        eventTypeService.delete(id);
        return "redirect:/events/" + userId;
    }


    @GetMapping("/{username}/{slug}")
    public String publicBookingPage(@PathVariable String username, @PathVariable String slug, Model model) {
        User user = userService.getByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Find specific event for this user
        EventType event = eventTypeService.getByUser(user.getId()).stream()
                .filter(e -> e.getSlug().equals(slug))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Event not found"));

        model.addAttribute("user", user);
        model.addAttribute("event", event);
        return "public-booking";
    }

    @PostMapping("/toggle/{id}")
    public String toggleStatus(@PathVariable Long id) {
        EventType event = eventTypeService.getById(id);
        event.setActive(!event.isActive());
        eventTypeService.save(event);
        return "redirect:/events/" + event.getUser().getId();
    }


    @GetMapping("/edit/{id}")
    public String editEvent(@PathVariable Long id, Model model) {
        EventType event = eventTypeService.getById(id);
        model.addAttribute("event", event);
        model.addAttribute("userId", event.getUser().getId());
        return "edit-event";
    }
}

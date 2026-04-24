package com.calclone.controller;

import com.calclone.entity.EventType;
import com.calclone.entity.User;
import com.calclone.repository.EventTypeRepository;
import com.calclone.repository.UserRepository;
import com.calclone.service.EventTypeService;
import com.calclone.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/events")
public class EventTypeController {

    private final EventTypeService eventTypeService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final EventTypeRepository eventTypeRepository;

    public EventTypeController(EventTypeService eventTypeService, UserService userService, UserRepository userRepository,
                               EventTypeRepository eventTypeRepository){
        this.eventTypeService = eventTypeService;
        this.userService = userService;
        this.userRepository = userRepository;
        this.eventTypeRepository = eventTypeRepository;
    }


    @GetMapping({"", "/"})
    public String events(Model model, @RequestParam(value = "keyword", required = false) String keyword) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        OAuth2User oauthUser = (OAuth2User) auth.getPrincipal();

        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setUsername(name);
                    return userRepository.save(newUser);
                });

        model.addAttribute("user", user);
        model.addAttribute("keyword", keyword);
//        model.addAttribute("events", eventTypeService.getByUser(user.getId()));

        model.addAttribute("events", keyword != null && !keyword.trim().isEmpty()
                ? eventTypeRepository.findByTitleContainingIgnoreCase(keyword)
                : eventTypeService.getByUser(user.getId()));

        return "events";
    }

    @GetMapping("/create/{userId}")
    public String createForm(@PathVariable Long userId, Model model) {
        model.addAttribute("event", new EventType());
        model.addAttribute("userId", userId);
        return "create-event";
    }

    @PostMapping("/create/{userId}")
    public String saveEvent(@PathVariable Long userId,
                            @ModelAttribute EventType eventType,
                            Model model) {

        User user = userService.getById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));


        if (eventTypeService.existsByUserIdAndSlug(userId, eventType.getSlug())) {
            String baseSlug = eventType.getSlug();
            int suffix = 1;
            while (eventTypeService.existsByUserIdAndSlug(userId, baseSlug + "-" + suffix)) {
                suffix++;
            }
            eventType.setSlug(baseSlug + "-" + suffix);
        }

        eventType.setUser(user);
        eventTypeService.save(eventType);
        return "redirect:/events";
    }

    @GetMapping("/{userId}")
    public String listEvents(@PathVariable Long userId, Model model) {
//        User user = userService.getById(userId);
        User user = userService.getById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("events", eventTypeService.getByUser(userId));
        model.addAttribute("user", user);
        model.addAttribute("userId", userId);
        return "events";
    }


    @GetMapping("/delete/{id}/{userId}")
    public String delete(@PathVariable Long id, @PathVariable Long userId) {
        eventTypeService.delete(id);
        return "redirect:/events/";
    }



    @GetMapping("/book/{username}/{slug}")
    public String publicBookingPage(@PathVariable String username,
                                    @PathVariable String slug, Model model) {
        User user = userService.getByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        EventType event = eventTypeService.getByUser(user.getId()).stream()
                .filter(e -> e.getSlug().equals(slug))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Event not found"));

        model.addAttribute("user", user);
        model.addAttribute("event", event);


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

    @PostMapping("/toggle/{id}")
    public String toggleStatus(@PathVariable Long id) {
        EventType event = eventTypeService.getById(id);
        event.setActive(!event.isActive());
        eventTypeService.save(event);
        return "redirect:/events/";
    }

    @GetMapping("/edit/{id}")
    public String editDetailed(@PathVariable Long id, Model model, HttpServletRequest request) {
        EventType event = eventTypeService.getById(id);
        model.addAttribute("event", event);
        model.addAttribute("user", event.getUser());

        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        model.addAttribute("baseUrl", baseUrl);

        return "edit-event";
    }

    @PostMapping("/update/{id}")
    public String updateEvent(@PathVariable Long id,
                              @ModelAttribute EventType updatedEvent) {

        EventType event = eventTypeService.getById(id);

        event.setTitle(updatedEvent.getTitle());
        event.setDescription(updatedEvent.getDescription());
        event.setSlug(updatedEvent.getSlug());
        event.setDuration(updatedEvent.getDuration());

        eventTypeService.save(event);

        return "redirect:/events/";
    }

    @GetMapping("/duplicate/{id}")
    public String duplicateEvent(@PathVariable Long id) {

        EventType event = eventTypeService.getById(id);

        EventType copy = new EventType();
        copy.setTitle(event.getTitle() + " Copy");
        copy.setDescription(event.getDescription());
        copy.setSlug(event.getSlug() + "-copy");
        copy.setDuration(event.getDuration());
        copy.setUser(event.getUser());

        eventTypeService.save(copy);

        return "redirect:/events/";
    }
}

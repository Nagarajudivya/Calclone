package com.calclone.controller;

import com.calclone.entity.EventType;
import com.calclone.service.EventTypeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PublicBookingController {

    private final EventTypeService eventTypeService;

    public PublicBookingController(EventTypeService eventTypeService){
        this.eventTypeService = eventTypeService;
    }

    @GetMapping("/book/{username}/{slug}")
    public String showPublicCalendar(@PathVariable String username, @PathVariable String slug, Model model) {
        EventType event = eventTypeService.findByUsernameAndSlug(username, slug);

        if (event == null || !event.isActive()) {
            return "error";
        }

        model.addAttribute("event", event);
        model.addAttribute("user", event.getUser());
        return "public-booking";
    }
}

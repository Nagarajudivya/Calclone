package com.calclone.controller;

import com.calclone.entity.User;
import com.calclone.service.UserService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAdvice {

    private final UserService userService;

    public GlobalModelAdvice(UserService userService) {
        this.userService = userService;
    }

    @ModelAttribute
    public void addUserToModel(Model model) {
        try {
            User currentUser = userService.getCurrentUser();
            model.addAttribute("user", currentUser);
        } catch (Exception ignored) {
        }
    }
}
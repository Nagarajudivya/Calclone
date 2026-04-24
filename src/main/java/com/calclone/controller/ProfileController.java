package com.calclone.controller;

import com.calclone.entity.User;
import com.calclone.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Controller
@RequestMapping("/profiles")
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String showProfile(Model model) {
        User currentUser = userService.getCurrentUser();
        model.addAttribute("user", currentUser);
        return "profiles";
    }


    @PostMapping("/update")
    public String updateProfile(
            @ModelAttribute("user") User updatedData,
            @RequestParam("avatarFile") MultipartFile file
    ) throws IOException {

        User existingUser = userService.getCurrentUser();


        existingUser.setUsername(updatedData.getUsername());
        existingUser.setFullName(updatedData.getFullName());
        existingUser.setBio(updatedData.getBio());


        if (!file.isEmpty()) {

//            String uploadDir = "uploads/";

            String uploadDir = System.getProperty("user.dir") + "/uploads/";

            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            file.transferTo(new File(uploadDir + fileName));

            existingUser.setAvatar("/uploads/" + fileName);
        }

        userService.saveProfile(existingUser);

        return "redirect:/profiles?success";
    }

    @PostMapping("/remove-avatar")
    public String removeAvatar() {

        User user = userService.getCurrentUser();
        user.setAvatar(null);

        userService.saveProfile(user);

        return "redirect:/profiles?removed";
    }

    @PostMapping("/delete-account")
    public String deleteAccount() {

        User user = userService.getCurrentUser();

        userService.deleteUser(user.getId());

        return "redirect:/login?deleted";
    }

}
package com.calclone.controller;

import com.calclone.entity.User;
import com.calclone.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class RegisterController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/register")
    public String registerForm() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String email,
                           @RequestParam String password,
                           Model model) {

        if (userRepository.findByEmail(email).isPresent()) {
            model.addAttribute("error", "Email already in use");
            return "register";
        }
        if (userRepository.findByUsername(username).isPresent()) {
            model.addAttribute("error", "Username already taken");
            return "register";
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);

        return "redirect:/login";
    }
}
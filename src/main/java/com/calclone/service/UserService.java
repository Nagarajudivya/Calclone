package com.calclone.service;

import com.calclone.entity.User;
import com.calclone.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> getByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User save(User user) {
        return userRepository.save(user);
    }


    public Optional<User> getById(Long id) {
        return userRepository.findById(id);
    }

    public User getLoggedInUser() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof OAuth2User)) {
            throw new RuntimeException("User not logged in");
        }

        OAuth2User oauthUser = (OAuth2User) auth.getPrincipal();
        String email = oauthUser.getAttribute("email");

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
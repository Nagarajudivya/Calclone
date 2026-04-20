//package com.calclone.service;
//
//import com.calclone.entity.User;
//import com.calclone.repository.UserRepository;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//
//@Service
//public class CustomUserDetailsService implements UserDetailsService {
//
//    private final UserRepository userRepository;
//    private final PasswordEncoder passwordEncoder;
//
//    public CustomUserDetailsService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
//        this.userRepository = userRepository;
//        this.passwordEncoder = passwordEncoder;
//    }
//
//    @Override
//    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new UsernameNotFoundException("No user found with email: " + email));
//
//        String password = user.getPassword() != null ? user.getPassword() : "";
//
//        return org.springframework.security.core.userdetails.User
//                .withUsername(user.getEmail())
//                .password(user.getPassword())
//                .roles("USER")
//                .build();
//    }
//}


package com.calclone.service;

import com.calclone.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        com.calclone.entity.User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No user with email: " + email));

        String password = user.getPassword() != null ? user.getPassword() : "";

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(password)
                .roles("USER")
                .build();
    }
}
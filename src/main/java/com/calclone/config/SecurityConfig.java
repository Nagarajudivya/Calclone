//package com.calclone.config;
//
//
//import com.calclone.service.CustomOAuth2UserService;
//import com.calclone.service.CustomUserDetailsService;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//
//@Configuration
//@EnableWebSecurity
//public class SecurityConfig {
//
//    private final CustomOAuth2UserService customOAuth2UserService;
//    private final CustomUserDetailsService customUserDetailsService;
//    private final PasswordEncoder passwordEncoder;
//
//    public SecurityConfig(CustomOAuth2UserService customOAuth2UserService, CustomUserDetailsService customUserDetailsService,
//                          PasswordEncoder passwordEncoder) {
//        this.customOAuth2UserService = customOAuth2UserService;
//        this.customUserDetailsService = customUserDetailsService;
//        this.passwordEncoder = passwordEncoder;
//    }
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/", "/login", "/css/**", "/js/**", "/events/book/**").permitAll()
//                        .anyRequest().authenticated()
//                )
////                .formLogin(form -> form
////                        .loginPage("/login")
////                        .loginProcessingUrl("/login")
////                        .defaultSuccessUrl("/events", true)
////                        .failureUrl("/login?error=true")
////                        .permitAll()
////                )
//
//                .oauth2Login(oauth -> oauth
//                        .loginPage("/login")
//                        .defaultSuccessUrl("/events", true)
//                        .userInfoEndpoint(userInfo -> userInfo
//                                .userService(customOAuth2UserService)
//                        )
//                )
//                .logout(logout -> logout
//                        .logoutSuccessUrl("/")
//                );
//
//        return http.build();
//    }
//}


package com.calclone.config;

import com.calclone.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    public SecurityConfig(@Lazy CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .userDetailsService(customUserDetailsService)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/register", "/css/**", "/js/**", "/events/book/**").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/events", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout.logoutSuccessUrl("/"));

        return http.build();
    }
}
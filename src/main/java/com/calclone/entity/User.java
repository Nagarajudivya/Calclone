package com.calclone.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    private String bio;

    private String timeZone;

    private String name;
    private String picture;

    @Column(unique = true)
    private String googleId;

    @Column
    private String password;

    @Column(length = 1000)
    private String googleAccessToken;
}

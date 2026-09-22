package com.rozgarmitra.in.DTOs.Response;

import com.rozgarmitra.in.Entity.Address;
import com.rozgarmitra.in.Enum.Role;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.Date;

@Data
public class UserResponseDto {
    private String id;
    private String name;
    private String username;
    private String email;
    private LocalDate dob;
    private String phone_number;
    private Address address;
    private Role role;
    private String token;

}

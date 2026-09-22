package com.rozgarmitra.in.DTOs.Request;

import com.rozgarmitra.in.Enum.Role;
import com.rozgarmitra.in.validation.ValidLabourAge;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;
import java.util.Date;

@Data

public class SignUpRequestDto {

    @NotBlank(message = "Name is Required")
    private String name;
    @NotBlank(message = "Userame is Required")
    @Column(unique = true)
    private String username;
    @Email(message = "Invalid Format")
    @NotBlank(message = "Email is Required")
    @Column(unique = true)
    private String email;
    @NotNull(message = "Age is Required")
    private LocalDate dob;
    @Pattern(regexp = "^\\+[1-9]\\d{6,14}$",
            message = "Phone must start with a '+' and country code, followed by 6-14 digits")
    @NotBlank(message = "Number is Required")
    private String phone_number;
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[@#$%^&+=!]).{8,}$",
            message = "Password must be at least 8 characters, contain a number, a letter, and a special character")
    @NotBlank(message = "Password cannot be empty")
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Valid
    @NotNull(message = "Address details are required")
    private AddressDto address;

}

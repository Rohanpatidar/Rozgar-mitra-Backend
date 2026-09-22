package com.rozgarmitra.in.DTOs.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequestDto {
    @NotBlank(message = "Username is Required")
    private String username;

    @NotBlank(message = "Password cannot be empty")
    private String password;
}

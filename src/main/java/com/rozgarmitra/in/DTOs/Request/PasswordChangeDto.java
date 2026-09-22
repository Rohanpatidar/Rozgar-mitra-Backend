package com.rozgarmitra.in.DTOs.Request;

import lombok.Data;

@Data
public class PasswordChangeDto {
    private String currentPassword;
    private String newPassword;
}
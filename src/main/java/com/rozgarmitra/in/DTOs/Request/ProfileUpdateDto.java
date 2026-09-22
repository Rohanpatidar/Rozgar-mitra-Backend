package com.rozgarmitra.in.DTOs.Request;

import lombok.Data;

@Data
public class ProfileUpdateDto {
    private String name;
    private String phone_number;
    private String addressText;
}

package com.rozgarmitra.in.DTOs.Request;

import lombok.Data;

@Data
public class TaskRequestDto {
    private Long serviceId;
    private Double latitude;
    private Double longitude;
    private String addressText;
}
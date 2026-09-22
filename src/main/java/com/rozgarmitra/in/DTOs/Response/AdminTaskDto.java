package com.rozgarmitra.in.DTOs.Response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminTaskDto {
    private Long id;
    private Long serviceId;
    private String serviceName;
    private String customerName;
    private String customerUsername;
    private String customerPhone;
    private String labourName;
    private String labourUsername;
    private String labourPhone;
    private String status;
    private String addressText;
    private Double estimatedPrice;
    private Double customerLatitude;
    private Double customerLongitude;
    private Double labourLatitude;
    private Double labourLongitude;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime startOtpGeneratedAt;
    private LocalDateTime startOtpVerifiedAt;
    private LocalDateTime completionOtpGeneratedAt;
    private LocalDateTime completionOtpVerifiedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Long durationMinutes;
    private Double finalAmount;
    private String paymentStatus;
    private LocalDateTime paidAt;
}
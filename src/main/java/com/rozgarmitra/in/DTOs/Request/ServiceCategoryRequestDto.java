package com.rozgarmitra.in.DTOs.Request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ServiceCategoryRequestDto {

    @NotBlank(message = "Service name is required")
    private String name;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Price per hour is required")
    @Min(value = 0, message = "Price cannot be negative")
    private Double pricePerHour;
}
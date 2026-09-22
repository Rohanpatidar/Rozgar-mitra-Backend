package com.rozgarmitra.in.DTOs.Request;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddressDto {

    @NotBlank(message = "Apartment number is required")
    private String apartmentNumber;

    @NotBlank(message = "Building name is required")
    private String buildingName;

    @NotBlank(message = "Colony is required")
    private String colony;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "Pincode is required")
    private String pincode;

    @NotBlank(message = "Country is required")
    private String country;
}
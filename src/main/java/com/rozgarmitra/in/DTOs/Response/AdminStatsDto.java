package com.rozgarmitra.in.DTOs.Response;

import lombok.Data;

@Data
public class AdminStatsDto {
    private long totalCustomers;
    private long totalLabours;
    private long activeLabours;
    private long totalTasks;
    private double platformRevenue;
}
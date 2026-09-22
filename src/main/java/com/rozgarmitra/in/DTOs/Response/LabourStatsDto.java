package com.rozgarmitra.in.DTOs.Response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LabourStatsDto {
    private long acceptedTasks;
    private double todayEarnings;
    private long totalLabours;
    private long activeLabours;
    public LabourStatsDto(long acceptedTasks, double todayEarnings) {
        this.acceptedTasks = acceptedTasks;
        this.todayEarnings = todayEarnings;
    }
    public LabourStatsDto(long totalLabours, long activeLabours) {
        this.totalLabours = totalLabours;
        this.activeLabours = activeLabours;
    }
}
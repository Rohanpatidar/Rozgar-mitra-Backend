package com.rozgarmitra.in.Controller;

import com.rozgarmitra.in.Entity.GeoLocation;
import com.rozgarmitra.in.Entity.User;
import com.rozgarmitra.in.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.rozgarmitra.in.Enum.Role;
import com.rozgarmitra.in.DTOs.Response.LabourStatsDto;
import com.rozgarmitra.in.service.TaskService;

@RestController
@RequestMapping("/api/labour")
public class LocationController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskService taskService;

    @PostMapping("/availability")
    public ResponseEntity<?> updateAvailability(@RequestBody AvailabilityRequest request,
            Authentication authentication) {
        User labour = userRepository.findByUsername(authentication.getName());
        if (labour == null || labour.getRole() != Role.ROLE_LABOUR) {
            return ResponseEntity.badRequest().body("Labour not found");
        }
        labour.setIsOnline(Boolean.TRUE.equals(request.online()));
        userRepository.save(labour);
        return ResponseEntity.ok(labour.getIsOnline());
    }

    @GetMapping("/availability")
    public ResponseEntity<Boolean> getAvailability(Authentication authentication) {
        User labour = userRepository.findByUsername(authentication.getName());
        return ResponseEntity.ok(labour != null && Boolean.TRUE.equals(labour.getIsOnline()));
    }

    @GetMapping("/stats")
    public ResponseEntity<LabourStatsDto> getStats(Authentication authentication) {
        return ResponseEntity.ok(taskService.getLabourStats(authentication.getName()));
    }

    @PostMapping("/update-location")
    public ResponseEntity<?> updateLabourLocation(@RequestBody GeoLocation location, Authentication authentication) {
        User labour = userRepository.findByUsername(authentication.getName());
        if (labour == null) {
            return ResponseEntity.badRequest().body("Labour not found");
        }

        labour.setCurrentLocation(location);
        userRepository.save(labour);

        return ResponseEntity.ok("Location updated successfully");
    }

    public record AvailabilityRequest(boolean online) {
    }
}
package com.rozgarmitra.in.Controller;

import com.rozgarmitra.in.DTOs.Request.ServiceCategoryRequestDto;
import com.rozgarmitra.in.DTOs.Response.UserResponseDto;
import com.rozgarmitra.in.DTOs.Response.AdminStatsDto;
import com.rozgarmitra.in.DTOs.Response.AdminTaskDto;
import com.rozgarmitra.in.Entity.ServiceCategory;
import com.rozgarmitra.in.Enum.Role;
import com.rozgarmitra.in.global.ApiResponse; // Assuming this is your global response format
import com.rozgarmitra.in.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Update based on your CorsConfig
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsDto> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }

    @GetMapping("/tasks")
    public ResponseEntity<List<AdminTaskDto>> getTasks(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(adminService.getTasks(search, page, size));
    }

    @GetMapping("/customers")
    public ResponseEntity<List<UserResponseDto>> getAllCustomers() {
        return ResponseEntity.ok(adminService.getUsersByRole(Role.ROLE_CUSTOMER));
    }

    @GetMapping("/labours")
    public ResponseEntity<List<UserResponseDto>> getAllLabours() {
        return ResponseEntity.ok(adminService.getUsersByRole(Role.ROLE_LABOUR));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable UUID id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully");
    }

    @GetMapping("/services")
    public ResponseEntity<List<ServiceCategory>> getAllServices(@RequestParam(required = false) String search) {
        return ResponseEntity.ok(adminService.searchServices(search));
    }

    @PostMapping("/services")
    public ResponseEntity<ServiceCategory> addService(@Valid @RequestBody ServiceCategoryRequestDto requestDto) {
        ServiceCategory newService = adminService.addService(requestDto);
        return new ResponseEntity<>(newService, HttpStatus.CREATED);
    }

    @PutMapping("/services/{id}")
    public ResponseEntity<ServiceCategory> updateService(
            @PathVariable Long id,
            @Valid @RequestBody ServiceCategoryRequestDto requestDto) {

        ServiceCategory updatedService = adminService.updateService(id, requestDto);
        return ResponseEntity.ok(updatedService);
    }

    @DeleteMapping("/services/{id}")
    public ResponseEntity<String> deleteService(@PathVariable Long id) {
        adminService.deleteService(id);
        return ResponseEntity.ok("Service deleted successfully");
    }
}
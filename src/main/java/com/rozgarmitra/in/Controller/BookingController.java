package com.rozgarmitra.in.Controller;

import com.rozgarmitra.in.DTOs.Request.TaskRequestDto;
import com.rozgarmitra.in.DTOs.Request.OtpVerificationDto;
import com.rozgarmitra.in.DTOs.Response.TaskResponseDto;
import com.rozgarmitra.in.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/booking")
public class BookingController {

    @Autowired
    private TaskService taskService;
    @PostMapping("/request")
    public ResponseEntity<TaskResponseDto> requestService(@RequestBody TaskRequestDto requestDto,
            Authentication authentication) {
        TaskResponseDto response = taskService.createTask(authentication.getName(), requestDto);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/nearby-tasks")
    public ResponseEntity<List<TaskResponseDto>> getNearbyTasks(@RequestParam(required = false) String search,
            Authentication authentication) {
        List<TaskResponseDto> tasks = taskService.getNearbyPendingTasks(authentication.getName(), search);
        return ResponseEntity.ok(tasks);
    }
    @PostMapping("/accept/{taskId}")
    public ResponseEntity<TaskResponseDto> acceptTask(@PathVariable Long taskId, Authentication authentication) {
        TaskResponseDto response = taskService.acceptTask(taskId, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reject/{taskId}")
    public ResponseEntity<TaskResponseDto> rejectTask(@PathVariable Long taskId, Authentication authentication) {
        TaskResponseDto response = taskService.rejectTask(taskId, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{taskId}/customer-reject")
    public ResponseEntity<TaskResponseDto> customerRejectTask(@PathVariable Long taskId,
            Authentication authentication) {
        return ResponseEntity.ok(taskService.customerRejectTask(taskId, authentication.getName()));
    }

    @GetMapping("/my-tasks")
    public ResponseEntity<List<TaskResponseDto>> getMyTasks(@RequestParam(required = false) String search,
            Authentication authentication) {
        List<TaskResponseDto> tasks = taskService.getMyTasks(authentication.getName(), search);
        return ResponseEntity.ok(tasks);
    }
    @GetMapping("/customer/bookings")
    public ResponseEntity<?> getCustomerBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            Authentication authentication) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        return ResponseEntity.ok(taskService.getCustomerBookings(authentication.getName(), pageable));
    }
    @PostMapping("/complete/{taskId}")
    public ResponseEntity<TaskResponseDto> completeTask(@PathVariable Long taskId, Authentication authentication) {
        TaskResponseDto response = taskService.completeTask(taskId, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{taskId}/start-otp")
    public ResponseEntity<TaskResponseDto> generateStartOtp(@PathVariable Long taskId, Authentication authentication) {
        return ResponseEntity.ok(taskService.generateStartOtp(taskId, authentication.getName()));
    }

    @PostMapping("/{taskId}/start-otp/verify")
    public ResponseEntity<TaskResponseDto> verifyStartOtp(@PathVariable Long taskId,
            @RequestBody OtpVerificationDto request, Authentication authentication) {
        return ResponseEntity.ok(taskService.verifyStartOtp(taskId, request.getOtp(), authentication.getName()));
    }

    @PostMapping("/{taskId}/completion-otp")
    public ResponseEntity<TaskResponseDto> generateCompletionOtp(@PathVariable Long taskId,
            Authentication authentication) {
        return ResponseEntity.ok(taskService.generateCompletionOtp(taskId, authentication.getName()));
    }

    @PostMapping("/{taskId}/completion-otp/verify")
    public ResponseEntity<TaskResponseDto> verifyCompletionOtp(@PathVariable Long taskId,
            @RequestBody OtpVerificationDto request, Authentication authentication) {
        return ResponseEntity.ok(taskService.verifyCompletionOtp(taskId, request.getOtp(), authentication.getName()));
    }

    @PostMapping("/{taskId}/pay")
    public ResponseEntity<TaskResponseDto> payForTask(@PathVariable Long taskId, Authentication authentication) {
        return ResponseEntity.ok(taskService.payForTask(taskId, authentication.getName()));
    }
}
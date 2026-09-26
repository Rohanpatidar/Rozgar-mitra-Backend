package com.rozgarmitra.in.Controller;

import com.rozgarmitra.in.Configuration.JwtUtils;
import com.rozgarmitra.in.DTOs.Request.ProfileUpdateDto;
import com.rozgarmitra.in.DTOs.Request.PasswordChangeDto;
import com.rozgarmitra.in.DTOs.Response.UserResponseDto;
import com.rozgarmitra.in.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.bind.annotation.DeleteMapping;


@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
public class ProfileController {
    private final UserService userService;
    private JwtUtils jwtUtils;

    @GetMapping("/profile")
    public ResponseEntity<UserResponseDto> getProfile(Authentication authentication) {
        return ResponseEntity.ok(userService.getProfile(authentication.getName()));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserResponseDto> updateProfile(
            @RequestBody ProfileUpdateDto request,
            Authentication authentication) {
        return ResponseEntity.ok(userService.updateProfile(authentication.getName(), request));
    }

    @PutMapping("/change-password")
    public ResponseEntity<String> changePassword(
            @RequestBody PasswordChangeDto request,
            Authentication authentication) {
        userService.changePassword(authentication.getName(), request);
        return ResponseEntity.ok("Password updated successfully");
    }



    @DeleteMapping("/delete-account")
    public ResponseEntity<?> deleteMyAccount(Authentication authentication) {
        try {
            userService.deleteAccount(authentication.getName());
            return ResponseEntity.ok().body("Account deleted successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting account.");
        }
    }
    }


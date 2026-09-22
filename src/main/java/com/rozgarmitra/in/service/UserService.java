package com.rozgarmitra.in.service;

import com.rozgarmitra.in.Configuration.JwtUtils;
import com.rozgarmitra.in.DTOs.Request.AddressDto;
import com.rozgarmitra.in.DTOs.Request.LoginRequestDto;
import com.rozgarmitra.in.DTOs.Request.ProfileUpdateDto;
import com.rozgarmitra.in.DTOs.Request.PasswordChangeDto;
import com.rozgarmitra.in.DTOs.Request.SignUpRequestDto;
import com.rozgarmitra.in.DTOs.Response.UserResponseDto;
import com.rozgarmitra.in.Entity.Address;
import com.rozgarmitra.in.Entity.User;
import com.rozgarmitra.in.Enum.Role;
import com.rozgarmitra.in.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtils jwtUtils;

    public UserResponseDto registerUser(SignUpRequestDto dto) {
        Address address = Address.builder().apartmentNumber(dto.getAddress().getApartmentNumber())
                .buildingName(dto.getAddress().getBuildingName())
                .colony(dto.getAddress().getColony())
                .city(dto.getAddress().getCity())
                .state(dto.getAddress().getState())
                .country(dto.getAddress().getCountry())
                .pincode(dto.getAddress().getPincode())
                .build();
        User user = new User();
        user.setName(dto.getName());
        user.setUsername(dto.getUsername());
        user.setDob(dto.getDob());
        user.setEmail(dto.getEmail());
        user.setPhone_number(dto.getPhone_number());
        user.setRole(dto.getRole());
        String password = passwordEncoder.encode(dto.getPassword());
        user.setPassword(password);
        user.setAddress(address);
        System.out.println("Role in DTO: " + dto.getRole());
        System.out.println("Role in Entity: " + user.getRole());

        User savedUser = userRepository.save(user);

        UserResponseDto responseDTO = new UserResponseDto();
        responseDTO.setId(savedUser.getId().toString());
        responseDTO.setName(savedUser.getName());
        responseDTO.setUsername(savedUser.getUsername());
        responseDTO.setEmail(savedUser.getEmail());
        responseDTO.setPhone_number(savedUser.getPhone_number());
        responseDTO.setAddress(savedUser.getAddress());
        responseDTO.setDob(savedUser.getDob());
        responseDTO.setRole(savedUser.getRole());

        return responseDTO;
    }

    public UserResponseDto loginUser(LoginRequestDto dto) {
        User user = userRepository.findByUsername(dto.getUsername());

        if (user == null) {
            throw new RuntimeException("User Not Found With this Username");
        }
        boolean isPasswordMatch = passwordEncoder.matches(dto.getPassword(), user.getPassword());

        if (!isPasswordMatch) {
            throw new RuntimeException("Invalid Password");
        }
        String token = jwtUtils.generateToken(user.getUsername(), user.getRole().name());
        UserResponseDto responseDto = new UserResponseDto();
        responseDto.setName(user.getName());
        responseDto.setUsername(user.getUsername());
        responseDto.setPhone_number(user.getPhone_number());
        responseDto.setEmail(user.getEmail());
        responseDto.setRole(user.getRole());
        responseDto.setDob(user.getDob());
        responseDto.setId(String.valueOf(user.getId()));
        responseDto.setToken(token);

        return responseDto;

    }

    public UserResponseDto getProfile(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        return toUserResponse(user);
    }

    public UserResponseDto updateProfile(String username, ProfileUpdateDto dto) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        if (dto.getName() != null && !dto.getName().isBlank()) {
            user.setName(dto.getName().trim());
        }
        if (dto.getPhone_number() != null && !dto.getPhone_number().isBlank()) {
            user.setPhone_number(dto.getPhone_number().trim());
        }
        if (dto.getAddressText() != null && !dto.getAddressText().isBlank()) {
            Address address = user.getAddress() == null ? new Address() : user.getAddress();
            address.setColony(dto.getAddressText().trim());
            user.setAddress(address);
        }
        return toUserResponse(userRepository.save(user));
    }

    public void changePassword(String username, PasswordChangeDto dto) {
        User user = userRepository.findByUsername(username);
        if (user == null)
            throw new RuntimeException("User not found");
        if (dto.getCurrentPassword() == null
                || !passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        if (dto.getNewPassword() == null
                || !dto.getNewPassword().matches("^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[@#$%^&+=!]).{8,}$")) {
            throw new IllegalArgumentException(
                    "New password must be at least 8 characters with a letter, number, and special character");
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }

    private UserResponseDto toUserResponse(User user) {
        UserResponseDto response = new UserResponseDto();
        response.setId(user.getId().toString());
        response.setName(user.getName());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setPhone_number(user.getPhone_number());
        response.setDob(user.getDob());
        response.setRole(user.getRole());
        response.setAddress(user.getAddress());
        return response;
    }

}
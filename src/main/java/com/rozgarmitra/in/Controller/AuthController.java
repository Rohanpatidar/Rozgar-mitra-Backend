package com.rozgarmitra.in.Controller;

import com.rozgarmitra.in.DTOs.Request.LoginRequestDto;
import com.rozgarmitra.in.DTOs.Request.SignUpRequestDto;
import com.rozgarmitra.in.DTOs.Response.UserResponseDto;
import com.rozgarmitra.in.Entity.User;
import com.rozgarmitra.in.Enum.Role;
import com.rozgarmitra.in.global.ApiResponse;
import com.rozgarmitra.in.repository.UserRepository;
import com.rozgarmitra.in.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/auth")
public class AuthController {
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/signup")
    public ResponseEntity<?> signupUser(
            @Valid @RequestBody
            SignUpRequestDto dto
            ){

        String requestedRole = dto.getRole().name().toUpperCase();
        try{
            if(requestedRole.equals("ROLE_ADMIN")){
                throw new Exception();
            }
        } catch (Exception e) {
            ApiResponse<UserResponseDto> response = new ApiResponse<>(HttpStatus.FORBIDDEN.value(),"Security Alert: You cannot create an Admin account directly!");
            return  ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        try{

       UserResponseDto savedResponse = userService.registerUser(dto);
       ApiResponse response = new ApiResponse<>(HttpStatus.CREATED.value(),"Registration SuccessFull...!" );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }catch (Exception e){
            ApiResponse response = new ApiResponse<>(HttpStatus.BAD_REQUEST.value(),e.getMessage() );
            e.printStackTrace();
            return  ResponseEntity.status( HttpStatus.BAD_REQUEST).body(response);
        }



    }
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserResponseDto>> loginUser(@Valid @RequestBody LoginRequestDto dto){
        try {
            UserResponseDto responseDto = userService.loginUser(dto);
            ApiResponse<UserResponseDto> response = new ApiResponse<>(
                    HttpStatus.OK.value(),
                    "Login Successfully",
                    responseDto
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<UserResponseDto> response = new ApiResponse<>(
                    HttpStatus.UNAUTHORIZED.value(),
                    e.getMessage() != null ? "Invalid Credentials" : "Invalid credentials"
            );
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

}

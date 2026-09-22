package com.rozgarmitra.in.Entity;

import com.rozgarmitra.in.Enum.Role;
import com.rozgarmitra.in.validation.ValidLabourAge;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "users", uniqueConstraints = {
                @UniqueConstraint(columnNames = { "username", "role" }),
                @UniqueConstraint(columnNames = { "email", "role" })
}, indexes = {
                @Index(name = "idx_users_username", columnList = "username"),
                @Index(name = "idx_users_role", columnList = "role"),
                @Index(name = "idx_users_role_location", columnList = "role, latitude, longitude")
})
@NoArgsConstructor
@AllArgsConstructor
@Data
@ValidLabourAge
public class User {
        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        private UUID id;
        @NotBlank(message = "Name is Required")
        private String name;
        @NotBlank(message = "Username is Required")
        private String username;
        @Email(message = "Invalid Format")
        @NotBlank(message = "Email is Required")
        private String email;
        @NotNull(message = "Age is Required")
        private LocalDate dob;
        @Pattern(regexp = "^\\+[1-9]\\d{6,14}$", message = "Phone must start with a '+' and country code, followed by 6-14 digits")
        @NotBlank(message = "Number is Required")
        private String phone_number;
        @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[@#$%^&+=!]).{8,}$", message = "Password must be at least 8 characters, contain a number, a letter, and a special character")
        @NotBlank(message = "Password cannot be empty")
        private String password;

        @NotNull
        @Enumerated(EnumType.STRING)
        private Role role;

        @Embedded
        private GeoLocation currentLocation;

        @Column(name = "is_online", nullable = false)
        private Boolean isOnline = false;

        @Embedded
        private Address address;

}

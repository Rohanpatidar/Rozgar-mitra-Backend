package com.rozgarmitra.in.service;

import com.rozgarmitra.in.Entity.Address;
import com.rozgarmitra.in.Entity.User;
import com.rozgarmitra.in.Enum.Role;
import com.rozgarmitra.in.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class AdminInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {

        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setName("Super Admin");
            admin.setUsername("admin"); // Aapka Hardcoded Username
            admin.setEmail("admin@rozgarmitra.com");
            admin.setPhone_number("+917389969550");
            admin.setDob(LocalDate.parse("2004-11-12"));
            admin.setPassword(passwordEncoder.encode("Admin@123")); // Aapka Hardcoded Password
            admin.setRole(Role.valueOf("ROLE_ADMIN")); // Ye role login response me aayega
            Address address = new Address();
            address.setCity("System");
            address.setCountry("India");
            admin.setAddress(address);

            userRepository.save(admin);
            System.out.println("Admin account created successfully! "+ admin);
        }
    }
}
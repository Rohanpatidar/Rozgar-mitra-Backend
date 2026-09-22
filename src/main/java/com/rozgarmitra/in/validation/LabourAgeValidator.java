package com.rozgarmitra.in.validation;

import com.rozgarmitra.in.DTOs.Request.SignUpRequestDto;
import com.rozgarmitra.in.Entity.User;
import com.rozgarmitra.in.Enum.Role;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.Period;

public class LabourAgeValidator implements ConstraintValidator<ValidLabourAge, User> {

    @Override
    public boolean isValid(User user, ConstraintValidatorContext context) {
        if (user == null || user.getRole() == null) {
            return true;
        }
        int age = Period.between(user.getDob(), LocalDate.now()).getYears();
        if (user.getRole() == Role.ROLE_LABOUR && age < 12) {
            return false; // Fails validation
        }

        return true; // Passes validation
    }
}
package dk.ek.gameapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

public record DesignerRequest(
        @NotBlank(message = "Name is required")
        String name,

        @Past(message = "Date of birth must be in the past")
        LocalDate dob
) {}

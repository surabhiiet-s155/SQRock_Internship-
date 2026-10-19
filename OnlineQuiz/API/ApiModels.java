package com.jobportal.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public final class ApiModels {
    private ApiModels() {
    }

    public record LoginRequest(
            @Email @NotBlank String email,
            @NotBlank String password,
            @NotBlank String role
    ) {
    }

    public record UserView(Long id, String name, String email, String role, String company) {
    }

    public record JobRequest(
            @NotBlank @Size(max = 120) String title,
            @NotBlank @Size(max = 120) String company,
            @NotBlank @Size(max = 120) String location,
            @NotBlank @Size(max = 40) String type,
            @NotBlank @Size(max = 80) String salary,
            @NotBlank @Size(max = 1200) String description,
            boolean remote
    ) {
    }

    public record JobView(
            Long id, String title, String company, String location, String type,
            String salary, String description, boolean remote, Instant createdAt
    ) {
    }

    public record ApplicationView(
            Long id, String jobTitle, String company, String candidateName,
            String candidateEmail, String status, Instant appliedAt
    ) {
    }

    public record Message(String message) {
    }
}

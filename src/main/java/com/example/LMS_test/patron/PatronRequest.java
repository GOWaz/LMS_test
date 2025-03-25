package com.example.LMS_test.patron;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PatronRequest {
    @NotBlank(message = "Name is required")
    private String name;
    private String contactInfo;
}

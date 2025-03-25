package com.example.LMS_test.book;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class BookRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Author is required")
    private String author;

    @Size(min = 13, max = 13, message = "ISBN must be exactly 13 characters long")
    @Pattern(regexp = "^[0-9]+$", message = "ISBN must contain only numbers")
    private String isbn;

    @NotNull(message = "Publish date is required")
    @PastOrPresent(message = "Publish date cannot be in the future")
    private LocalDate publishDate;
}

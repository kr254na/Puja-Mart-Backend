package com.krishna.Pujamart.feedback.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateReviewRequest {

    private UUID productId;

    private UUID kitId;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating cannot exceed 5")
    private Integer rating;

    @NotBlank(message = "Review title is required")
    @Size(max = 100, message = "Title cannot exceed 100 characters")
    private String title;

    @NotBlank(message = "Review comment is required")
    @Size(max = 1000, message = "Comment cannot exceed 1000 characters")
    private String comment;
}
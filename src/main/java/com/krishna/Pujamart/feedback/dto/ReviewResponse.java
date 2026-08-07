package com.krishna.Pujamart.feedback.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ReviewResponse {
    private UUID id;
    private UUID userId;
    private String reviewerName;
    private Integer rating;
    private String title;
    private String comment;
    private boolean verifiedPurchase;
    private LocalDateTime createdAt;
}
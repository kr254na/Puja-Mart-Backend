package com.krishna.Pujamart.feedback.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class ReviewSummaryResponse {
    private Double averageRating;
    private Long totalReviews;
    private Map<Integer, Long> ratingBreakdown; // Key: Star (1-5), Value: Count
}

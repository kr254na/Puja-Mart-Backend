package com.krishna.Pujamart.identity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GoogleLoginRequest {
    @NotBlank(message = "Authorization code cannot be blank")
    private String code;
}
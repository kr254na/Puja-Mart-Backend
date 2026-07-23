package com.krishna.Pujamart.identity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

    @NotBlank(message = "Email or phone number is required")
    private String identifier;

    @NotBlank(message = "Password is required")
    private String password;
}

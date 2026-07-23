package com.krishna.Pujamart.identity.dto;

import com.krishna.Pujamart.identity.enums.Role;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RegistrationResponse {
    private UserResponse userResponse;
}

package com.krishna.Pujamart.identity.dto;

import com.krishna.Pujamart.identity.enums.Role;
import lombok.*;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String contact;
    private Role role;
}
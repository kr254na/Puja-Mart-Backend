package com.krishna.Pujamart.identity.model;

import com.krishna.Pujamart.identity.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
   private UUID id;
    @Column(nullable = false, length = 50)
    @NotBlank
    @Size(min = 2, max = 50)
   private String firstName;
    @Column(nullable = false,length = 50)
    @NotBlank
    @Size(min = 2, max = 50)
   private String lastName;
    @Column(nullable = false, unique = true, length = 255)
    @Email
    @NotBlank
   private String email;
    @Column(unique = true)
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Enter a valid 10-digit mobile number")
    private String contact;
    @Column(nullable = true)
    private String password;
    @Column(nullable = false)
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private Role role = Role.ROLE_CUSTOMER;
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    @Builder.Default
    @Column(nullable = false)
    private boolean emailVerified = false;
    @Builder.Default
    @Column(nullable = false)
    private boolean contactVerified = false;
    @Builder.Default
    @Column(nullable = false)
    private boolean enabled = true;
    @Builder.Default
    @Column(nullable = false)
    private boolean accountNonLocked = true;
    @Builder.Default
    @Column(nullable = false)
    private boolean accountNonExpired = true;
    @Builder.Default
    @Column(nullable = false)
    private boolean credentialsNonExpired = true;
}

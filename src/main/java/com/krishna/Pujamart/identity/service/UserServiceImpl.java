package com.krishna.Pujamart.identity.service;

import com.krishna.Pujamart.identity.dto.*;
import com.krishna.Pujamart.identity.exception.*;
import com.krishna.Pujamart.identity.model.User;
import com.krishna.Pujamart.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public ApiResponse<UserResponse> getProfile(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return ApiResponse.<UserResponse>builder()
                .success(true)
                .message("Profile retrieved successfully")
                .data(mapToUserResponse(user))
                .build();
    }

    @Override
    @Transactional
    public ApiResponse<UserResponse> updateProfile(String email, ProfileUpdateRequest request) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (request.getContact() != null && !request.getContact().equals(user.getContact())) {
            if (userRepository.existsByContact(request.getContact())) {
                throw new DuplicateContactException("Contact number is already in use by another account");
            }
        }

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }

        if (request.getContact() != null) {
            user.setContact(request.getContact());
        }

        userRepository.save(user);

        return ApiResponse.<UserResponse>builder()
                .success(true)
                .message("Profile updated successfully")
                .data(mapToUserResponse(user))
                .build();
    }

    @Override
    @Transactional
    public ApiResponse<String> changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new PasswordMismatchException("Your current password choice is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return ApiResponse.<String>builder()
                .success(true)
                .message("Password changed successfully")
                .build();
    }

    @Override
    @Transactional
    public ApiResponse<String> deleteAccount(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        userRepository.delete(user);

        return ApiResponse.<String>builder()
                .success(true)
                .message("Account deleted successfully")
                .build();
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .contact(user.getContact())
                .role(user.getRole())
                .build();
    }
}

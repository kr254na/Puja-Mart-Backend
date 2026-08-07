package com.krishna.Pujamart.identity.service;

import com.krishna.Pujamart.cart.repository.CartRepository;
import com.krishna.Pujamart.identity.dto.*;
import com.krishna.Pujamart.identity.exception.*;
import com.krishna.Pujamart.identity.model.User;
import com.krishna.Pujamart.identity.repository.RefreshTokenRepository;
import com.krishna.Pujamart.identity.repository.UserRepository;
import com.krishna.Pujamart.identity.utility.UserMapper;
import com.krishna.Pujamart.wishlist.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CartRepository cartRepository;
    private final WishlistRepository wishlistRepository;

    @Override
    public ApiResponse<UserResponse> getProfile(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return ApiResponse.success("Profile retrieved successfully",userMapper.toUserResponse(user));
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

        return ApiResponse.success("Profile updated successfully",userMapper.toUserResponse(user));
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

        return ApiResponse.success("Password changed successfully");
    }

    @Override
    @Transactional
    public ApiResponse<String> deleteAccount(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // 1. Delete refresh tokens
        refreshTokenRepository.deleteByUser(user);

        // 2. Delete Cart & Wishlist
        cartRepository.findByUserId(user.getId()).ifPresent(cartRepository::delete);
        wishlistRepository.findByUserId(user.getId()).ifPresent(wishlistRepository::delete);

        // 3. Soft Delete / Anonymize User
        user.setEnabled(false);
        user.setFirstName("Deleted");
        user.setLastName("User");
        user.setEmail("deleted-" + user.getId() + "@pujamart.local");
        user.setContact(null);
        user.setPassword(null);

        userRepository.save(user);

        return ApiResponse.success("Account deleted successfully");
    }

}

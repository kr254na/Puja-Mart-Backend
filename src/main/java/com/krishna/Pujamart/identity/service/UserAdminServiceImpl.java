package com.krishna.Pujamart.identity.service;

import com.krishna.Pujamart.identity.dto.ApiResponse;
import com.krishna.Pujamart.identity.dto.UserResponse;
import com.krishna.Pujamart.identity.exception.UserNotFoundException;
import com.krishna.Pujamart.identity.model.User;
import com.krishna.Pujamart.identity.repository.UserRepository;
import com.krishna.Pujamart.identity.utility.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserAdminServiceImpl implements UserAdminService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public ApiResponse<Page<UserResponse>> getAllUsers(Pageable pageable) {
        Page<UserResponse> usersPage = userRepository.findAll(pageable)
                .map(userMapper::toUserResponse);
        return ApiResponse.success("Fetched all users successfully",usersPage);
    }

    @Override
    public ApiResponse<UserResponse> getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        return ApiResponse.success("User details retrieved successfully",userMapper.toUserResponse(user));
    }

    @Override
    @Transactional
    public ApiResponse<String> toggleUserStatus(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        boolean currentStatus = user.isAccountNonLocked();
        user.setAccountNonLocked(!currentStatus);
        userRepository.save(user);

        String message = !currentStatus ? "Account unlocked successfully" : "Account locked successfully";

        return ApiResponse.success(message);
    }

}

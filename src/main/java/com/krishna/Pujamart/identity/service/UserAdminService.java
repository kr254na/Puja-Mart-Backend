package com.krishna.Pujamart.identity.service;

import com.krishna.Pujamart.identity.dto.ApiResponse;
import com.krishna.Pujamart.identity.dto.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public interface UserAdminService {

    ApiResponse<Page<UserResponse>> getAllUsers(Pageable pageable);

    ApiResponse<UserResponse> getUserById(UUID userId);

    ApiResponse<String> toggleUserStatus(UUID userId);
}

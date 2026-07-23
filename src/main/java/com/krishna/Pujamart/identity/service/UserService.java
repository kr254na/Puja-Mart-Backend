package com.krishna.Pujamart.identity.service;

import com.krishna.Pujamart.identity.dto.*;

public interface UserService {
    ApiResponse<UserResponse> getProfile(String email);
    ApiResponse<UserResponse> updateProfile(String email, ProfileUpdateRequest request);
    ApiResponse<String> changePassword(String email, ChangePasswordRequest request);
    ApiResponse<String> deleteAccount(String email);
}

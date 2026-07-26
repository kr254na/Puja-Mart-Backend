package com.krishna.Pujamart.identity.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.krishna.Pujamart.identity.dto.*;
import com.krishna.Pujamart.identity.enums.Role;
import com.krishna.Pujamart.identity.exception.DuplicateContactException;
import com.krishna.Pujamart.identity.exception.DuplicateEmailException;
import com.krishna.Pujamart.identity.exception.RefreshTokenException;
import com.krishna.Pujamart.identity.model.RefreshToken;
import com.krishna.Pujamart.identity.model.User;
import com.krishna.Pujamart.identity.repository.RefreshTokenRepository;
import com.krishna.Pujamart.identity.repository.UserRepository;
import com.krishna.Pujamart.identity.utility.JwtUtil;
import com.krishna.Pujamart.identity.utility.UserMapper;
import com.krishna.Pujamart.identity.utility.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserMapper userMapper;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;
    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String googleRedirectUri;

    @Transactional
    public ApiResponse<RegistrationResponse> register(RegistrationRequest request) {
        if(userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new DuplicateEmailException("Email already exists");
        }
        if(request.getContact()!=null && userRepository.existsByContact(request.getContact())) {
            throw new DuplicateContactException("Contact already exists");
        }
        User user = User.builder()
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .contact(request.getContact())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();
        userRepository.save(user);
        UserResponse userResponse = userMapper.toUserResponse(user);
        RegistrationResponse registrationResponse = RegistrationResponse
                .builder()
                .userResponse(userResponse)
                .build();
        return ApiResponse.success("Registration Successful",registrationResponse);
    }

    @Transactional
    public ApiResponse<LoginResponse> login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getIdentifier(),
                        request.getPassword()
                )
        );
        String accessToken = jwtUtil.generateAccessToken(authentication);
        String refreshToken = jwtUtil.generateRefreshToken(authentication);

        UserPrincipal userPrincipal =
                (UserPrincipal) authentication.getPrincipal();

        User user = userPrincipal.getUser();
        UserResponse userResponse =
                userMapper.toUserResponse(user);

        saveRefreshToken(user,refreshToken);

        LoginResponse loginResponse = LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(userResponse)
                .build();

        return ApiResponse.success("Login Successful",loginResponse);
    }

    @Transactional(noRollbackFor = RefreshTokenException.class)
    public ApiResponse<LoginResponse> refresh(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        if (!jwtUtil.validateRefreshToken(refreshToken)) {
            throw new RefreshTokenException("Invalid refresh token");
        }
        RefreshToken storedToken = refreshTokenRepository.findByTokenWithUser(refreshToken)
                .orElseThrow(() -> new RefreshTokenException("Refresh token not found"));
        if (storedToken.getExpiryDate().isBefore(Instant.now())) {
            throw new RefreshTokenException("Refresh token expired");
        }
        if(storedToken.isRevoked()) {
            User user = storedToken.getUser();
            refreshTokenRepository.revokeAllByUser(user);
            throw new RefreshTokenException("Security Alert: Token has already been used. All sessions have been terminated.");
        }


        User user = storedToken.getUser();
        UserPrincipal userPrincipal = UserPrincipal.builder().user(user).build();

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        user.getEmail(),
                        null,
                        userPrincipal.getAuthorities()
                );

        String newAccessToken = jwtUtil.generateAccessToken(authentication);

        UserResponse userResponse = userMapper.toUserResponse(user);

        storedToken.setRevoked(true);

        String newRefreshToken = jwtUtil.generateRefreshToken(authentication);

        saveRefreshToken(user,newRefreshToken);

        return ApiResponse.success("New Access Token generated successfully",
                LoginResponse.builder()
                        .accessToken(newAccessToken)
                        .refreshToken(newRefreshToken)
                        .user(userResponse)
                        .build());
    }

    @Transactional(noRollbackFor = RefreshTokenException.class)
    public ApiResponse<String> logout(RefreshTokenRequest refreshTokenRequest) {
        String refreshToken = refreshTokenRequest.getRefreshToken();
        if (!jwtUtil.validateRefreshToken(refreshToken)) {
            throw new RefreshTokenException("Invalid refresh token");
        }
        RefreshToken storedToken = refreshTokenRepository.findByTokenWithUser(refreshToken)
                .orElseThrow(() -> new RefreshTokenException("Refresh token not found"));
        if (storedToken.getExpiryDate().isBefore(Instant.now())) {
            throw new RefreshTokenException("Refresh token expired");
        }
        if(storedToken.isRevoked()) {
            User user = storedToken.getUser();
            refreshTokenRepository.revokeAllByUser(user);
            throw new RefreshTokenException("Security Alert: Token has already been used. All sessions have been terminated.");
        }
        storedToken.setRevoked(true);
        return ApiResponse.success("Logged out successfully");
    }

    @Transactional
    public ApiResponse<LoginResponse> loginWithGoogle(GoogleLoginRequest request) {
        try {
            GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance(),
                    "https://oauth2.googleapis.com/token",
                    googleClientId,
                    googleClientSecret,
                    request.getCode(),
                    googleRedirectUri
            ).execute();

            GoogleIdToken idToken = tokenResponse.parseIdToken();
            GoogleIdToken.Payload payload = idToken.getPayload();

            String email = payload.getEmail();
            String firstName = (String) payload.get("given_name");
            String lastName = (String) payload.get("family_name");

            User user = userRepository.findByEmailIgnoreCase(email).orElseGet(() -> {
                User newUser = User.builder()
                        .email(email)
                        .firstName(firstName != null ? firstName : "GoogleUser")
                        .lastName(lastName != null ? lastName : "")
                        .role(Role.ROLE_CUSTOMER)
                        .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                        .accountNonLocked(true)
                        .build();
                return userRepository.save(newUser);
            });

            UserPrincipal userPrincipal = UserPrincipal.builder().user(user).build();
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    user.getEmail(),
                    null,
                    userPrincipal.getAuthorities()
            );

            String accessToken = jwtUtil.generateAccessToken(authentication);
            String refreshToken = jwtUtil.generateRefreshToken(authentication);

            saveRefreshToken(user, refreshToken);

            UserResponse userResponse = userMapper.toUserResponse(user);

            LoginResponse loginResponse = LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .user(userResponse)
                    .build();
            return ApiResponse.success("Google login successful",loginResponse);
        } catch (IOException e) {
            throw new BadCredentialsException(
                    "Failed to verify authentication with Google");
        }
    }


    private void saveRefreshToken(User user, String token) {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .user(user)
                .expiryDate(Instant.now().plusMillis(refreshTokenExpiration))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);
    }

}
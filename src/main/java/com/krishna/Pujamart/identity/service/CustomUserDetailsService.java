package com.krishna.Pujamart.identity.service;

import com.krishna.Pujamart.identity.model.User;
import com.krishna.Pujamart.identity.repository.UserRepository;
import com.krishna.Pujamart.identity.utility.UserPrincipal;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = null;
        if (username.contains("@")) {
            user = userRepository.findByEmailIgnoreCase(username)
                    .orElseThrow(() ->
                            new UsernameNotFoundException("User not found"));
        } else {
            user = userRepository.findByContact(username)
                    .orElseThrow(() ->
                            new UsernameNotFoundException("User not found"));
        }
        return UserPrincipal
                .builder()
                .user(user)
                .build();
    }
}

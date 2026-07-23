package com.krishna.Pujamart.identity.filter;

import com.krishna.Pujamart.identity.service.CustomUserDetailsService;
import com.krishna.Pujamart.identity.utility.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@AllArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String authHeader = request.getHeader("Authorization");
            String token = null, username = null;
            if(authHeader!=null && authHeader.startsWith("Bearer ")){
                token = authHeader.substring(7);
                username = jwtUtil.extractUsername(token);
            }
            if(username!=null && SecurityContextHolder.getContext().getAuthentication()==null) {
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
                if(jwtUtil.validateToken(username,token,userDetails)) {
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                    );
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }
        }
        catch(JwtException jwtException) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");

            response.getWriter().write(String.format("""
            {
                "success": false,
                "message": "Invalid or expired access token",
                "timestamp": "%s"
            }
            """, LocalDateTime.now()));
            return;
        }
        catch (UsernameNotFoundException usernameNotFoundException) {

        }
        filterChain.doFilter(request,response);
    }
}

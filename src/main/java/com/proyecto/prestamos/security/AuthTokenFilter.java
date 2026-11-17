package com.proyecto.prestamos.security;

import java.io.IOException;

import io.jsonwebtoken.Claims;

import com.proyecto.prestamos.service.CustomUserDetailsService;
import com.proyecto.prestamos.service.TokenRevocationService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;


 //autentica solo JWT válidos cuya identificación jti no este revocada.
 //se registra en la cadena de Spring Security.
 
public class AuthTokenFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final TokenRevocationService tokenRevocationService;

    public AuthTokenFilter(JwtUtil jwtUtil,
                           CustomUserDetailsService userDetailsService,
                           TokenRevocationService tokenRevocationService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.tokenRevocationService = tokenRevocationService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String jwt = parseJwt(request);
        try {
            if (jwt != null) {
                
                Claims claims = jwtUtil.getClaims(jwt);
                if (!tokenRevocationService.isRevoked(claims.getId())) {
                    String username = claims.getSubject();
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            
            SecurityContextHolder.clearContext();
        }

        // spring security responde 401 si el endpoint necesita autenticacion
        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        return authorization != null && authorization.startsWith("Bearer ")
                ? authorization.substring(7)
                : null;
    }
}

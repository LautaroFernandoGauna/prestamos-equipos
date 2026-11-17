package com.proyecto.prestamos.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.proyecto.prestamos.service.CustomUserDetailsService;
import com.proyecto.prestamos.service.TokenRevocationService;

@Configuration
public class WebSecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final TokenRevocationService tokenRevocationService;
    private final JwtUtil jwtUtil;
    private final AuthEntryPointJwt unauthorizedHandler;

    public WebSecurityConfig(CustomUserDetailsService userDetailsService,
                             TokenRevocationService tokenRevocationService,
                             JwtUtil jwtUtil,
                             AuthEntryPointJwt unauthorizedHandler) {
        this.userDetailsService = userDetailsService;
        this.tokenRevocationService = tokenRevocationService;
        this.jwtUtil = jwtUtil;
        this.unauthorizedHandler = unauthorizedHandler;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // API REST sin sesiones, autenticada por Bearer JWT.
                .cors(cors -> cors.disable()) // Conserva la configuración previa de la demo.
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.POST, "/api/auth/signin", "/api/auth/signup").permitAll()
                        .requestMatchers("/api/test/all", "/h2-console/**", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/error").permitAll()
    // solo el admin puede cambiar perfiles
                        .requestMatchers(HttpMethod.PUT, "/solicitantes/*/perfil").hasRole("ADMIN")
                        .anyRequest().authenticated()
);

        http.addFilterBefore(
                new AuthTokenFilter(jwtUtil, userDetailsService, tokenRevocationService),
                UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}

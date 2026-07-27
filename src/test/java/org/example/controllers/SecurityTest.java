package org.example.controllers;

import org.example.config.SecurityConfig;
import org.example.dto.ExchangeRateResponseDto;
import org.example.security.JpaUserDetailService;
import org.example.security.JwtToken;
import org.example.services.ExchangeRateService;
import org.example.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.*;

@WebMvcTest
@Import({SecurityConfig.class, JwtToken.class})
@ActiveProfiles("test")
public class SecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtToken jwtToken;

    @MockitoBean
    private JpaUserDetailService userDetailService;

    @MockitoBean
    private ExchangeRateService exchangeRateService;

    @MockitoBean
    private UserService userService;

    @Test
    void dataLoadingWithoutTokenTest() throws Exception {
        mockMvc.perform(post("/api/v1/admin/rates/upload"))
                .andExpect(status().isForbidden());
    }

    @Test
    void dataLoadingAsUserTest() throws Exception {
        UserDetails user = org.springframework.security.core.userdetails.User.builder()
                .username("admin")
                .password("admin123")
                .roles("USER").build();
        when(userDetailService.loadUserByUsername("admin")).thenReturn(user);

        String token = jwtToken.generateToken(user);

        mockMvc.perform(post("/api/v1/admin/rates/upload").header("Authorization", "Bearer "+token))
                .andExpect(status().isForbidden());
    }

    @Test
    void dataLoadingAsAdminTest() throws Exception {
        UserDetails admin = org.springframework.security.core.userdetails.User.builder()
                .username("admin")
                .password("admin123")
                .roles("ADMIN").build();
        when(userDetailService.loadUserByUsername("admin")).thenReturn(admin);

        String token = jwtToken.generateToken(admin);

        mockMvc.perform(post("/api/v1/admin/rates/upload").header("Authorization", "Bearer "+token))
                .andExpect(status().isOk());
    }

    @Test
    void getCurrencyPairValidRequestTest() throws Exception {
        when(exchangeRateService.getCurrencyPair(eq("USD"), any()))
                .thenReturn(new ExchangeRateResponseDto(1L, "USD/BYN", BigDecimal.valueOf(3.24), LocalDate.now()));

        mockMvc.perform(get("/api/v1/rate").param("code", "USD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("USD/BYN"));
    }

    @Test
    void loginWithWrongPasswordTest() throws Exception {
        when(userService.login("admin", "wrongPassword")).thenThrow(BadCredentialsException.class);

        mockMvc.perform(post("/api/v1/login")
                        .param("username", "admin")
                        .param("password", "wrongPassword"))
                .andExpect(status().isUnauthorized());
    }
}
package com.msp.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msp.enums.EUserRole;
import com.msp.payloads.dtos.UserDto;
import com.msp.payloads.response.AuthResponse;
import com.msp.services.AuthService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private AuthService authService;
    @Test
    void testSignUpHandler_Success() throws Exception {
        UserDto requestDto = new UserDto();
        requestDto.setEmail("test@example.com");
        requestDto.setPassword("Password123!");
        requestDto.setRole(EUserRole.ROLE_STORE_MANAGER);

        AuthResponse mockResponse = new AuthResponse();
        mockResponse.setJwt("mock-jwt-token-123");

        Mockito.when(authService.signup(any(UserDto.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwt").value("mock-jwt-token-123"));
    }

    @Test
    void testLoginHandler_Success() throws Exception {
        UserDto requestDto = new UserDto();
        requestDto.setEmail("test@example.com");
        requestDto.setPassword("Password123!");

        AuthResponse mockResponse = new AuthResponse();
        mockResponse.setJwt("mock-jwt-token-456");

        Mockito.when(authService.login(any(UserDto.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwt").value("mock-jwt-token-456"));
    }
}
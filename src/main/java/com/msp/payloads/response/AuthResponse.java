package com.msp.payloads.response;

import com.msp.payloads.dtos.UserDto;
import lombok.Data;

@Data
public class AuthResponse {
    private String jwt;
    private String message;
    private UserDto user;
}

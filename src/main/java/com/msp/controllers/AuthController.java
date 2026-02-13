package com.msp.controllers;

import com.msp.exceptions.UserException;
import com.msp.payloads.dtos.UserDto;
import com.msp.payloads.response.AuthResponse;
import com.msp.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authsService;

    //http://localhost:5000/msp/api/auth/signup
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signupHandler(
            @RequestBody UserDto userDto
            ) throws UserException {
        return ResponseEntity.ok(
                authsService.signup(userDto)
        );
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginHandler(
            @RequestBody UserDto userDto
    ) throws UserException {
        return ResponseEntity.ok(
                authsService.login(userDto)
        );
    }

}

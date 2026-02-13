package com.msp.controllers;

import com.msp.exceptions.UserException;
import com.msp.mappers.UserMapper;
import com.msp.models.User;
import com.msp.payloads.dtos.UserDto;
import com.msp.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserDto> getUserProfile(
            @RequestHeader("Authorization") String token
    ) {
        User user = userService.getCurrentUserFromToken(token);
        return ResponseEntity.ok(UserMapper.toDTO(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(
            @RequestHeader("Authorization") String token,
            @PathVariable UUID id
    ) throws UserException {
        User user = userService.getUserById(id);
        if(user==null) {
            throw new UserException("User Not Found!");
        }
        return ResponseEntity.ok(UserMapper.toDTO(user));
    }

}

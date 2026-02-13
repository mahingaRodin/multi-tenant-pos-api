package com.msp.services;

import com.msp.models.User;

import java.util.List;
import java.util.UUID;

public interface UserService {
    User getCurrentUserFromToken(String token);
    User getCurrentUser();
    User getUserByEmail(String email);
    User getUserById(UUID id);
    List<User> getAllUsers();
}

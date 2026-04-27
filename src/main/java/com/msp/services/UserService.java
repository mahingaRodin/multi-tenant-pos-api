package com.msp.services;

import com.msp.enums.EUserStatus;
import com.msp.exceptions.UserException;
import com.msp.models.User;
import com.msp.payloads.dtos.UpdateUserDto;
import com.msp.payloads.dtos.UserDto;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface UserService {

    User createUser(UserDto dto) throws UserException;

    User getCurrentUserFromToken(String token) throws UserException;
    User getCurrentUser() throws UserException;
    User getUserByEmail(String email) throws UserException;
    User getUserById(UUID id) throws UserException;

    Page<UserDto> getAllUsers(int page, int size, String sortBy, EUserStatus status);

    User updateUser(UUID id, UpdateUserDto dto) throws UserException;

    User dischargeUser(UUID id, String reason) throws UserException;
    User suspendUser(UUID id, String reason) throws UserException;
    User activateUser(UUID id) throws UserException;

    void deleteUser(UUID id) throws UserException;

    boolean canDeleteUser(UUID id);
}
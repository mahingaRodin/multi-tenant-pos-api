package com.msp.controllers;

import com.msp.exceptions.UserException;
import com.msp.mappers.UserMapper;
import com.msp.models.User;
import com.msp.payloads.dtos.UpdateUserDto;
import com.msp.payloads.dtos.UserDto;
import com.msp.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Endpoints for managing user profiles and information")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {
    private final UserService userService;

    @Operation(
            summary = "Get all users with pagination",
            description = "Retrieves a paginated list of all users in the system. Supports pagination and sorting parameters. Requires SUPER_ADMIN role."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Users retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Insufficient privileges",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content
            )
    })
    @GetMapping
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<Page<UserDto>> getAllUsers(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Field to sort by", example = "email")
            @RequestParam(defaultValue = "id") String sortBy
    ) {
        Page<UserDto> usersPage = userService.getAllUsers(page, size, sortBy);
        return ResponseEntity.ok(usersPage);
    }

    @Operation(
            summary = "Get current user profile",
            description = "Retrieves the profile information of the currently authenticated user based on the provided JWT token"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User profile retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found for the provided token",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content
            )
    })
    @GetMapping("/profile")
    public ResponseEntity<UserDto> getUserProfile(
            @Parameter(
                    description = "Bearer JWT token for authentication",
                    required = true,
                    example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                    schema = @Schema(type = "string", format = "Bearer [token]")
            )
            @RequestHeader("Authorization") String token
    ) {
        User user = userService.getCurrentUserFromToken(token);
        return ResponseEntity.ok(UserMapper.toDTO(user));
    }

    @Operation(
            summary = "Get user by ID",
            description = "Retrieves user information by their unique identifier. Requires appropriate permissions."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid user ID format",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Insufficient permissions to view this user",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found with the given ID",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(
            @Parameter(
                    description = "Bearer JWT token for authentication",
                    required = true,
                    example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                    schema = @Schema(type = "string", format = "Bearer [token]")
            )
            @RequestHeader("Authorization") String token,

            @Parameter(
                    name = "id",
                    description = "UUID of the user to retrieve",
                    required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @PathVariable UUID id
    ) throws UserException {
        User user = userService.getUserById(id);
        if (user == null) {
            throw new UserException("User Not Found!");
        }
        return ResponseEntity.ok(UserMapper.toDTO(user));
    }

    // CREATE
    @Operation(summary = "Create new user", description = "Creates a new user account. Requires SUPER_ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_ADMIN"),
            @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    @PostMapping
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<UserDto> createUser(
            @RequestBody @Valid UserDto userDto
    ) throws UserException {
        User created = userService.createUser(userDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserMapper.toDTO(created));
    }

    // UPDATE
    @Operation(summary = "Update user", description = "Updates an existing user's information. Requires SUPER_ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_ADMIN"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateUserDto userDto
    ) throws UserException {
        User updated = userService.updateUser(id, userDto);
        return ResponseEntity.ok(UserMapper.toDTO(updated));
    }

    // DELETE
    @Operation(summary = "Delete user", description = "Deletes a user by ID. Requires SUPER_ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_ADMIN"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<Void> deleteUser(
            @PathVariable UUID id
    ) throws UserException {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

//    @Operation(summary = "Assign role to user", description = "Changes a user's role. Requires SUPER_ADMIN role.")
//    @ApiResponses({
//            @ApiResponse(responseCode = "200", description = "Role assigned successfully"),
//            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_ADMIN"),
//            @ApiResponse(responseCode = "404", description = "User not found")
//    })
//    @PatchMapping("/{id}/role")
//    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
//    public ResponseEntity<UserDto> assignRole(
//            @PathVariable UUID id,
//            @RequestParam String role
//    ) throws UserException {
//        User updated = userService.assignRole(id, role);
//        return ResponseEntity.ok(UserMapper.toDTO(updated));
//    }
}
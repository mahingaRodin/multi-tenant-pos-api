package com.msp.payloads.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserDto {
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    private String firstName;
    private String lastName;
    private String phone;

    @NotNull(message = "Role is required")
    private String role;

    private String branchId;
    private String storeId;
}

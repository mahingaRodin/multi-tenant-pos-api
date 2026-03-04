package com.msp.models;

import com.msp.enums.EUserRole;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import jakarta.validation.constraints.Email;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@Table(name = "users")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    @Email(message = "Email Should be valid!")
    private String email;
    private String phone;

    @ManyToOne
    @JsonIgnoreProperties({ "storeAdmin" })
    private Store store;

    @ManyToOne
    @JsonIgnoreProperties({ "manager", "store" })
    private Branch branch;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EUserRole role;

    @Column(nullable = false)
    private String password;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLogin;

}

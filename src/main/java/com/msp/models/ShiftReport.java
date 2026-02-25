package com.msp.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@Table(name = "shift_reports")
public class ShiftReport {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


}

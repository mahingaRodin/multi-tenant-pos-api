package com.msp.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@Table(name = "inventories")
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    private Branch branch;

    @ManyToOne
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    private LocalTime lastUpdate;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        lastUpdate = LocalTime.now();
    }

}

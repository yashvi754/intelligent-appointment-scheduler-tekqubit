package com.appointmentscheduler.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "parts_inventory")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartsInventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "part_id")
    private Integer partId;

    @ManyToOne
    @JoinColumn(name = "center_id", nullable = false)
    private ServiceCenter serviceCenter;

    @Column(name = "part_name")
    private String partName;

    @Column(name = "available_parts")
    private Integer availableParts;

    @Column(name = "ordered_parts")
    private Integer orderedParts;

    @Column(name = "lead_time_days")
    @Builder.Default
    private Integer leadTimeDays = 2; // Default from Diagram
}
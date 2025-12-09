package com.appointmentscheduler.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "service_parts_mapping")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServicePartsMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceCatalog serviceCatalog;

    @ManyToOne
    @JoinColumn(name = "part_id", nullable = false)
    private PartsInventory partsInventory;

    @Column(name = "quantity_required")
    private Integer quantityRequired;
}
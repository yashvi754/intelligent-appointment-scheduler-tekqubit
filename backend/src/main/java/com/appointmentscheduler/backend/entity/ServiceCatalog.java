package com.appointmentscheduler.backend.entity;

import com.appointmentscheduler.backend.enums.BayType;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "service_catalog")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceCatalog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "service_id")
    private Integer serviceId;

    private String name;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "required_skill_level")
    private Integer requiredSkillLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "required_bay_type")
    private BayType requiredBayType;

    // Mapped relationships
    @OneToMany(mappedBy = "serviceCatalog")
    private List<ServicePartsMapping> requiredParts;

    @OneToMany(mappedBy = "serviceCatalog")
    private List<SymptomDataset> symptoms;
}
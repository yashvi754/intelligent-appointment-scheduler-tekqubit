package com.appointmentscheduler.backend.entity;

import com.appointmentscheduler.backend.enums.RegionStrategy;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "service_centers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceCenter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "center_id")
    private Integer centerId;

    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "region_strategy")
    private RegionStrategy regionStrategy;

    // Relationships
    @OneToMany(mappedBy = "serviceCenter")
    private List<ServiceBay> bays;

    @OneToMany(mappedBy = "serviceCenter")
    private List<PartsInventory> inventory;

    @OneToMany(mappedBy = "serviceCenter")
    private List<Employee> employees;
}
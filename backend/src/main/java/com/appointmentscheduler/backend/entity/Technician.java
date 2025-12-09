package com.appointmentscheduler.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "technicians")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Technician {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tech_id")
    private Long technicianId;

    private String name;

    @Column(name = "skill_level")
    private Integer skillLevel;

    @ManyToOne(optional = false)
    @JoinColumn(name = "center_id", nullable = false)
    private ServiceCenter serviceCenter;

    // Link back to Employee login (optional for now)
    @OneToOne
    @JoinColumn(name = "emp_id")
    private Employee employee;

    @OneToMany(mappedBy = "assignedTech")
    private List<AppointmentResources> assignments;
}

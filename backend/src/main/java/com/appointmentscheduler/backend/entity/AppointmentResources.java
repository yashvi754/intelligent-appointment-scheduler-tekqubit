package com.appointmentscheduler.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "appointment_resources")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentResources {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointments appointment;

    @ManyToOne
    @JoinColumn(name = "assigned_tech_id", nullable = false)
    private Technician assignedTech;

    @ManyToOne
    @JoinColumn(name = "assigned_bay_id", nullable = false)
    private ServiceBay assignedBay;
}
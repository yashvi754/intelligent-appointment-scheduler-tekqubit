package com.appointmentscheduler.backend.entity;

import com.appointmentscheduler.backend.enums.EmployeeRole;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "emp_id")
    private Integer empId;

    @ManyToOne
    @JoinColumn(name = "center_id", nullable = false)
    private ServiceCenter serviceCenter;

    private String username;

    @Enumerated(EnumType.STRING)
    private EmployeeRole role;
    
    // If this employee is a technician, this link exists
    @OneToOne(mappedBy = "employee")
    private Technician technicianProfile;
}
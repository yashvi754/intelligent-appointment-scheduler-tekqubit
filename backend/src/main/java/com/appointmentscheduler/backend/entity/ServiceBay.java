package com.appointmentscheduler.backend.entity;

import com.appointmentscheduler.backend.enums.BayType;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "service_bays")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceBay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bay_id")
    private Long bayId;

    private String name;

    @Enumerated(EnumType.STRING)
    private BayType type;

    @ManyToOne(optional = false)
    @JoinColumn(name = "center_id", nullable = false)
    private ServiceCenter serviceCenter;

    @OneToMany(mappedBy = "assignedBay")
    private List<AppointmentResources> assignments;
}

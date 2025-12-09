package com.appointmentscheduler.backend.entity;

import com.appointmentscheduler.backend.enums.ProcurementStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "procurement_queue")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcurementQueue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id")
    private Integer taskId;

    @ManyToOne
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointments appointment;

    @ManyToOne
    @JoinColumn(name = "part_id", nullable = false)
    private PartsInventory part;

    @Column(name = "needed_by_date")
    private LocalDateTime neededByDate;

    @Enumerated(EnumType.STRING)
    private ProcurementStatus status;
}
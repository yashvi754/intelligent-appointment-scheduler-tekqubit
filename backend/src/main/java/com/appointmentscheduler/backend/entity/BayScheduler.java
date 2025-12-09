package com.appointmentscheduler.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "bay_scheduler")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BayScheduler {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "bay_id", nullable = false)
    private ServiceBay bay;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "bitmask", nullable = false)
    private Integer bitmask;
}

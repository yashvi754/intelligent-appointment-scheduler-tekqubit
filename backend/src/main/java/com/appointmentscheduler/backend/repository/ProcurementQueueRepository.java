package com.appointmentscheduler.backend.repository;

import com.appointmentscheduler.backend.entity.ProcurementQueue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcurementQueueRepository extends JpaRepository<ProcurementQueue, Integer> {
}

package com.appointmentscheduler.backend.repository;

import com.appointmentscheduler.backend.entity.BayScheduler;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface BaySchedulerRepository extends JpaRepository<BayScheduler, Long> {
    Optional<BayScheduler> findByBay_BayIdAndDate(Long bayId, LocalDate date);
}

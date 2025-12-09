package com.appointmentscheduler.backend.repository;

import com.appointmentscheduler.backend.entity.Appointments;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentsRepository extends JpaRepository<Appointments, Integer> {
}

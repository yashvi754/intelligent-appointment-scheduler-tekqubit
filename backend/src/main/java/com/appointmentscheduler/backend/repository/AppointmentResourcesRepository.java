package com.appointmentscheduler.backend.repository;

import com.appointmentscheduler.backend.entity.AppointmentResources;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentResourcesRepository extends JpaRepository<AppointmentResources, Integer> {
}

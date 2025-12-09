package com.appointmentscheduler.backend.repository;

import com.appointmentscheduler.backend.entity.ServiceCenter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceCenterRepository extends JpaRepository<ServiceCenter, Integer> {
}

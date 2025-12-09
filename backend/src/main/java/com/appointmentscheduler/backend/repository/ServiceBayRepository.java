package com.appointmentscheduler.backend.repository;

import com.appointmentscheduler.backend.entity.ServiceBay;
import com.appointmentscheduler.backend.enums.BayType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceBayRepository extends JpaRepository<ServiceBay, Long> {
    List<ServiceBay> findByServiceCenter_CenterIdAndType(Integer centerId, BayType type);
}

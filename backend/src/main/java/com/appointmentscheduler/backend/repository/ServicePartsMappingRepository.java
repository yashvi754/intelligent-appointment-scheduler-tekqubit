package com.appointmentscheduler.backend.repository;

import com.appointmentscheduler.backend.entity.ServicePartsMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicePartsMappingRepository extends JpaRepository<ServicePartsMapping, Integer> {
    List<ServicePartsMapping> findByServiceCatalog_ServiceId(Integer serviceId);
}

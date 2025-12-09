package com.appointmentscheduler.backend.repository;

import com.appointmentscheduler.backend.entity.ServiceCatalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceCatalogRepository extends JpaRepository<ServiceCatalog, Integer> {
}

package com.appointmentscheduler.backend.repository;

import com.appointmentscheduler.backend.entity.PartsInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PartsInventoryRepository extends JpaRepository<PartsInventory, Integer> {
    Optional<PartsInventory> findByServiceCenter_CenterIdAndPartName(Integer centerId, String partName);
}

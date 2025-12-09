package com.appointmentscheduler.backend.controller;

import com.appointmentscheduler.backend.entity.*;
import com.appointmentscheduler.backend.repository.*;
import com.appointmentscheduler.backend.service.AppointmentBookingService;
import com.appointmentscheduler.backend.service.BitmaskSchedulerService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
// import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
public class SchedulerController {

    private final ServiceCatalogRepository serviceCatalogRepository;
    private final ServicePartsMappingRepository servicePartsMappingRepository;
    private final PartsInventoryRepository partsInventoryRepository;
    private final TechnicianRepository technicianRepository;
    private final ServiceBayRepository serviceBayRepository;
    private final ServiceCenterRepository serviceCenterRepository;
    private final BitmaskSchedulerService bitmaskSchedulerService;
    private final AppointmentBookingService appointmentBookingService;

    @PostMapping("/find-slot")
    public ResponseEntity<?> findSlot(@RequestBody FindSlotRequest request) {
        try {
            // 1. Validate inputs
            Optional<ServiceCatalog> serviceOpt = serviceCatalogRepository.findById(request.getServiceCatalogId());
            if (serviceOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Service not found with ID: " + request.getServiceCatalogId()));
            }

            Optional<ServiceCenter> centerOpt = serviceCenterRepository.findById(request.getCenterId());
            if (centerOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Service center not found with ID: " + request.getCenterId()));
            }

            ServiceCatalog service = serviceOpt.get();
            ServiceCenter center = centerOpt.get();

            // 2. Dynamic Inventory Check
            LocalDateTime partsArrivalDate = calculatePartsArrivalDate(service, request.getCenterId());

            // 3. Dynamic Resource Lookup
            List<Technician> qualifiedTechs = technicianRepository
                .findByServiceCenter_CenterIdAndSkillLevelGreaterThanEqual(
                    request.getCenterId(), 
                    service.getRequiredSkillLevel()
                );

            if (qualifiedTechs.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("No qualified technicians found for skill level: " + service.getRequiredSkillLevel()));
            }

            List<ServiceBay> qualifiedBays = serviceBayRepository
                .findByServiceCenter_CenterIdAndType(
                    request.getCenterId(), 
                    service.getRequiredBayType()
                );

            if (qualifiedBays.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("No qualified service bays found for type: " + service.getRequiredBayType()));
            }

            // 4. Algorithm Execution
            LocalDateTime earliestSlot = bitmaskSchedulerService.findEarliestSlot(
                partsArrivalDate,
                service.getDurationMinutes(),
                qualifiedTechs,
                qualifiedBays
            );

            if (earliestSlot == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("No available slots found in the next 30 days"));
            }

            return ResponseEntity.ok(new FindSlotResponse(earliestSlot, partsArrivalDate));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Error finding slot: " + e.getMessage()));
        }
    }

    @PostMapping("/book")
    public ResponseEntity<?> book(@RequestBody BookAppointmentRequest request) {
        try {
            LocalDateTime startTime = request.getStartTime();
            if (startTime == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("startTime is required"));
            }

            Appointments appointment = appointmentBookingService.bookAppointment(
                    request.getCustomerId(),
                    request.getVehicleId(),
                    request.getServiceCatalogId(),
                    request.getCenterId(),
                    startTime,
                    request.isEmergency()
            );

            return ResponseEntity.ok(appointment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error booking appointment: " + e.getMessage()));
        }
    }

    /**
     * Calculate parts arrival date based on inventory availability
     */
    private LocalDateTime calculatePartsArrivalDate(ServiceCatalog service, Integer centerId) {
        // Fetch all required parts for this service
        List<ServicePartsMapping> requiredParts = servicePartsMappingRepository
            .findByServiceCatalog_ServiceId(service.getServiceId());

        if (requiredParts.isEmpty()) {
            // No parts required, can schedule immediately
            return LocalDateTime.now();
        }

        int maxLeadTime = 0;
        boolean allPartsAvailable = true;

        for (ServicePartsMapping mapping : requiredParts) {
            PartsInventory part = mapping.getPartsInventory();
            
            // Get the part inventory for this specific center
            Optional<PartsInventory> centerPartOpt = partsInventoryRepository
                .findByServiceCenter_CenterIdAndPartName(centerId, part.getPartName());

            if (centerPartOpt.isEmpty()) {
                // Part not found in center inventory, use default lead time
                maxLeadTime = Math.max(maxLeadTime, part.getLeadTimeDays() != null ? part.getLeadTimeDays() : 2);
                allPartsAvailable = false;
                continue;
            }

            PartsInventory centerPart = centerPartOpt.get();
            int quantityRequired = mapping.getQuantityRequired() != null ? mapping.getQuantityRequired() : 1;

            // Check if part is available
            if (centerPart.getAvailableParts() < quantityRequired) {
                // Part not available, need to wait for lead time
                int leadTime = centerPart.getLeadTimeDays() != null ? centerPart.getLeadTimeDays() : 2;
                maxLeadTime = Math.max(maxLeadTime, leadTime);
                allPartsAvailable = false;
            }
        }

        if (allPartsAvailable) {
            // All parts available, can schedule immediately
            return LocalDateTime.now();
        }

        // Calculate arrival date: Now + MaxLeadTime + 1 day buffer
        return LocalDateTime.now().plusDays(maxLeadTime + 1);
    }

    // Request DTO
    @Data
    static class FindSlotRequest {
        private Integer serviceCatalogId;
        private Integer centerId;
    }

    @Data
    static class BookAppointmentRequest {
        private Integer customerId;
        private Integer vehicleId;
        private Integer serviceCatalogId;
        private Integer centerId;
        private LocalDateTime startTime;
        private boolean emergency;
    }

    // Response DTO
    @Data
    static class FindSlotResponse {
        private LocalDateTime earliestSlot;
        private LocalDateTime partsArrivalDate;

        public FindSlotResponse(LocalDateTime earliestSlot, LocalDateTime partsArrivalDate) {
            this.earliestSlot = earliestSlot;
            this.partsArrivalDate = partsArrivalDate;
        }
    }

    // Error Response DTO
    @Data
    static class ErrorResponse {
        private String message;

        public ErrorResponse(String message) {
            this.message = message;
        }
    }
}

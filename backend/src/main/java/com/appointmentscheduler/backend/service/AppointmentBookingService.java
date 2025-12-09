package com.appointmentscheduler.backend.service;

import com.appointmentscheduler.backend.entity.*;
import com.appointmentscheduler.backend.enums.AppointmentStatus;
import com.appointmentscheduler.backend.enums.ProcurementStatus;
import com.appointmentscheduler.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AppointmentBookingService {

    private final AppointmentsRepository appointmentsRepository;
    private final AppointmentResourcesRepository appointmentResourcesRepository;
    private final ProcurementQueueRepository procurementQueueRepository;
    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final ServiceCatalogRepository serviceCatalogRepository;
    private final ServiceCenterRepository serviceCenterRepository;
    private final ServicePartsMappingRepository servicePartsMappingRepository;
    private final PartsInventoryRepository partsInventoryRepository;
    private final TechnicianRepository technicianRepository;
    private final ServiceBayRepository serviceBayRepository;
    private final TechnicianSchedulerRepository technicianSchedulerRepository;
    private final BaySchedulerRepository baySchedulerRepository;
    private final BitmaskSchedulerService bitmaskSchedulerService;

    private static final int TOTAL_SLOTS = 18;
    private static final LocalTime DAY_START = LocalTime.of(9, 0);

    @Transactional
    public Appointments bookAppointment(Integer customerId,
                                        Integer vehicleId,
                                        Integer serviceCatalogId,
                                        Integer centerId,
                                        LocalDateTime requestedStartTime,
                                        boolean emergency) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found: " + vehicleId));

        if (!vehicle.getCustomer().getCustomerId().equals(customer.getCustomerId())) {
            throw new IllegalArgumentException("Vehicle does not belong to customer");
        }

        ServiceCatalog service = serviceCatalogRepository.findById(serviceCatalogId)
                .orElseThrow(() -> new IllegalArgumentException("Service not found: " + serviceCatalogId));

        ServiceCenter center = serviceCenterRepository.findById(centerId)
                .orElseThrow(() -> new IllegalArgumentException("Service center not found: " + centerId));

        int requiredSlots = (int) Math.ceil((double) service.getDurationMinutes() / 30);

        List<Technician> qualifiedTechs = technicianRepository
                .findByServiceCenter_CenterIdAndSkillLevelGreaterThanEqual(centerId, service.getRequiredSkillLevel());
        List<ServiceBay> qualifiedBays = serviceBayRepository
                .findByServiceCenter_CenterIdAndType(centerId, service.getRequiredBayType());

        BitmaskSchedulerService.ScheduledAssignment assignment = bitmaskSchedulerService.findEarliestAssignment(
                requestedStartTime,
                service.getDurationMinutes(),
                qualifiedTechs,
                qualifiedBays
        );

        if (assignment == null) {
            throw new IllegalStateException("Requested time slot is no longer available");
        }

        Technician selectedTech = assignment.technician();
        ServiceBay selectedBay = assignment.bay();
        LocalDate date = assignment.startTime().toLocalDate();
        int slotIndex = assignment.slotIndex();

        if (slotIndex < 0 || slotIndex + requiredSlots > TOTAL_SLOTS) {
            throw new IllegalStateException("Calculated time slot is outside working hours");
        }

        int techMask = bitmaskSchedulerService.getTechnicianBitmask(selectedTech.getTechnicianId(), date);
        int bayMask = bitmaskSchedulerService.getBayBitmask(selectedBay.getBayId(), date);

        techMask = markBusy(techMask, requiredSlots, slotIndex);
        bayMask = markBusy(bayMask, requiredSlots, slotIndex);

        upsertTechSchedule(selectedTech, date, techMask);
        upsertBaySchedule(selectedBay, date, bayMask);

        List<ServicePartsMapping> requiredParts = servicePartsMappingRepository
                .findByServiceCatalog_ServiceId(service.getServiceId());

        List<ProcurementQueue> procurementTasks = new ArrayList<>();
        boolean anyMissingParts = false;

        for (ServicePartsMapping mapping : requiredParts) {
            PartsInventory templatePart = mapping.getPartsInventory();
            Optional<PartsInventory> centerPartOpt = partsInventoryRepository
                    .findByServiceCenter_CenterIdAndPartName(centerId, templatePart.getPartName());
            if (centerPartOpt.isEmpty()) {
                anyMissingParts = true;
                PartsInventory partForQueue = templatePart;
                ProcurementQueue task = ProcurementQueue.builder()
                        .appointment(null)
                        .part(partForQueue)
                        .neededByDate(requestedStartTime)
                        .status(ProcurementStatus.ACTION_REQUIRED)
                        .build();
                procurementTasks.add(task);
                continue;
            }

            PartsInventory centerPart = centerPartOpt.get();
            int quantityRequired = mapping.getQuantityRequired() != null ? mapping.getQuantityRequired() : 1;
            int available = centerPart.getAvailableParts() != null ? centerPart.getAvailableParts() : 0;
            int ordered = centerPart.getOrderedParts() != null ? centerPart.getOrderedParts() : 0;

            if (available >= quantityRequired) {
                centerPart.setAvailableParts(available - quantityRequired);
            } else {
                anyMissingParts = true;
                int shortage = quantityRequired - available;
                centerPart.setAvailableParts(0);
                centerPart.setOrderedParts(ordered + shortage);
                ProcurementQueue task = ProcurementQueue.builder()
                        .appointment(null)
                        .part(centerPart)
                        .neededByDate(requestedStartTime)
                        .status(ProcurementStatus.ACTION_REQUIRED)
                        .build();
                procurementTasks.add(task);
            }

            partsInventoryRepository.save(centerPart);
        }

        LocalDateTime scheduledStartTime = assignment.startTime();
        LocalDateTime endTime = scheduledStartTime.plusMinutes(service.getDurationMinutes());

        Appointments appointment = Appointments.builder()
                .customer(customer)
                .vehicle(vehicle)
                .serviceCatalog(service)
            .startTime(scheduledStartTime)
                .endTime(endTime)
                .status(anyMissingParts ? AppointmentStatus.PENDING_PARTS : AppointmentStatus.CONFIRMED)
                .isEmergency(emergency)
                .build();

        appointment = appointmentsRepository.save(appointment);

        for (ProcurementQueue task : procurementTasks) {
            task.setAppointment(appointment);
        }
        if (!procurementTasks.isEmpty()) {
            procurementQueueRepository.saveAll(procurementTasks);
        }

        AppointmentResources resources = AppointmentResources.builder()
                .appointment(appointment)
                .assignedTech(selectedTech)
                .assignedBay(selectedBay)
                .build();
        appointmentResourcesRepository.save(resources);

        return appointment;
    }

    private boolean isRangeFree(int mask, int requiredSlots, int slotIndex) {
        int targetMask = (1 << requiredSlots) - 1;
        return ((mask >> slotIndex) & targetMask) == 0;
    }

    private int markBusy(int mask, int requiredSlots, int slotIndex) {
        int targetMask = (1 << requiredSlots) - 1;
        targetMask = targetMask << slotIndex;
        return mask | targetMask;
    }

    private void upsertTechSchedule(Technician tech, LocalDate date, int newMask) {
        Optional<TechnicianScheduler> existing = technicianSchedulerRepository
                .findByTechnician_TechnicianIdAndDate(tech.getTechnicianId(), date);
        TechnicianScheduler scheduler = existing.orElseGet(() -> TechnicianScheduler.builder()
                .technician(tech)
                .date(date)
                .bitmask(0)
                .build());
        scheduler.setBitmask(newMask);
        technicianSchedulerRepository.save(scheduler);
    }

    private void upsertBaySchedule(ServiceBay bay, LocalDate date, int newMask) {
        Optional<BayScheduler> existing = baySchedulerRepository
                .findByBay_BayIdAndDate(bay.getBayId(), date);
        BayScheduler scheduler = existing.orElseGet(() -> BayScheduler.builder()
                .bay(bay)
                .date(date)
                .bitmask(0)
                .build());
        scheduler.setBitmask(newMask);
        baySchedulerRepository.save(scheduler);
    }
}

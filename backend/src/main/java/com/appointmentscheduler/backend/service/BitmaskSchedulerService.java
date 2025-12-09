package com.appointmentscheduler.backend.service;

import com.appointmentscheduler.backend.entity.ServiceBay;
import com.appointmentscheduler.backend.entity.Technician;
import com.appointmentscheduler.backend.entity.BayScheduler;
import com.appointmentscheduler.backend.entity.TechnicianScheduler;
import com.appointmentscheduler.backend.repository.BaySchedulerRepository;
import com.appointmentscheduler.backend.repository.TechnicianSchedulerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor // Automatically injects the Repositories
public class BitmaskSchedulerService {

    private final TechnicianSchedulerRepository technicianSchedulerRepository;
    private final BaySchedulerRepository baySchedulerRepository;

    // Configuration: 9:00 AM to 6:00 PM = 9 Hours = 18 Slots (30 mins each)
    private static final int TOTAL_SLOTS = 18;
    private static final LocalTime DAY_START = LocalTime.of(9, 0);

    /**
     * MAIN ALGORITHM WITH DATABASE INTEGRATION
     * Returns only the earliest slot start time (kept for backward compatibility).
     */
    public LocalDateTime findEarliestSlot(
            LocalDateTime partsArrivalTime,
            int serviceDurationMinutes,
            List<Technician> qualifiedTechs,
            List<ServiceBay> qualifiedBays
    ) {
        ScheduledAssignment assignment = findEarliestAssignment(partsArrivalTime, serviceDurationMinutes, qualifiedTechs, qualifiedBays);
        return assignment != null ? assignment.startTime() : null;
    }

    /**
     * Find earliest slot and also the concrete technician and bay assignment.
     */
    public ScheduledAssignment findEarliestAssignment(
            LocalDateTime partsArrivalTime,
            int serviceDurationMinutes,
            List<Technician> qualifiedTechs,
            List<ServiceBay> qualifiedBays
    ) {
        int requiredSlots = (int) Math.ceil((double) serviceDurationMinutes / 30);

        LocalDate today = LocalDate.now();

        for (int dayOffset = 0; dayOffset < 30; dayOffset++) {
            LocalDate currentDay = today.plusDays(dayOffset);

            if (currentDay.isBefore(partsArrivalTime.toLocalDate())) {
                continue;
            }

            int startSlotIndex = 0;
            if (currentDay.isEqual(partsArrivalTime.toLocalDate())) {
                startSlotIndex = calculateSlotIndex(partsArrivalTime.toLocalTime());
                if (startSlotIndex >= TOTAL_SLOTS) {
                    continue;
                }
            }
            if (startSlotIndex < 0) {
                startSlotIndex = 0;
            }

            int bestSlotForDay = Integer.MAX_VALUE;
            Technician bestTechForDay = null;
            ServiceBay bestBayForDay = null;
            boolean foundOnThisDay = false;

            for (Technician tech : qualifiedTechs) {
                int techMask = getTechnicianBitmask(tech.getTechnicianId(), currentDay);

                for (ServiceBay bay : qualifiedBays) {
                    int bayMask = getBayBitmask(bay.getBayId(), currentDay);

                    int combinedMask = techMask | bayMask;

                    int validSlot = findConsecutiveZeros(combinedMask, requiredSlots, startSlotIndex);

                    if (validSlot != -1) {
                        if (validSlot < bestSlotForDay) {
                            bestSlotForDay = validSlot;
                            bestTechForDay = tech;
                            bestBayForDay = bay;
                            foundOnThisDay = true;

                            if (bestSlotForDay == startSlotIndex) {
                                return new ScheduledAssignment(
                                        mapSlotToDateTime(currentDay, bestSlotForDay),
                                        bestTechForDay,
                                        bestBayForDay,
                                        bestSlotForDay
                                );
                            }
                        }
                    }
                }
            }

            if (foundOnThisDay && bestTechForDay != null && bestBayForDay != null) {
                return new ScheduledAssignment(
                        mapSlotToDateTime(currentDay, bestSlotForDay),
                        bestTechForDay,
                        bestBayForDay,
                        bestSlotForDay
                );
            }
        }

        return null;
    }

    /**
     * BITWISE HELPER: Finds k consecutive zeros
     */
    private int findConsecutiveZeros(int mask, int k, int startSearchFrom) {
        int targetMask = (1 << k) - 1;
        for (int i = startSearchFrom; i <= (TOTAL_SLOTS - k); i++) {
            if (((mask >> i) & targetMask) == 0) {
                return i;
            }
        }
        return -1;
    }

    public int calculateSlotIndex(LocalTime time) {
        if (time.isBefore(DAY_START)) return 0;
        long minutesDiff = ChronoUnit.MINUTES.between(DAY_START, time);
        return (int) Math.ceil((double) minutesDiff / 30);
    }

    private LocalDateTime mapSlotToDateTime(LocalDate date, int slotIndex) {
        return date.atTime(DAY_START).plusMinutes(slotIndex * 30L);
    }

    // =================================================================
    // DATABASE HELPERS (Replacing the Mock)
    // =================================================================

    public int getTechnicianBitmask(Long techId, LocalDate date) {
        Optional<TechnicianScheduler> schedule = 
            technicianSchedulerRepository.findByTechnician_TechnicianIdAndDate(techId, date);
        
        // If present, return the bitmask. If not found (no schedule created yet), assume 0 (Free).
        return schedule.map(TechnicianScheduler::getBitmask).orElse(0);
    }

    public int getBayBitmask(Long bayId, LocalDate date) {
        Optional<BayScheduler> schedule = 
            baySchedulerRepository.findByBay_BayIdAndDate(bayId, date);
        
        return schedule.map(BayScheduler::getBitmask).orElse(0);
    }

    public record ScheduledAssignment(
            LocalDateTime startTime,
            Technician technician,
            ServiceBay bay,
            int slotIndex
    ) {}
}
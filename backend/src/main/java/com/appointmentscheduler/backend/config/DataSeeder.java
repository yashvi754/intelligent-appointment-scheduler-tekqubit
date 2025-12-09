package com.appointmentscheduler.backend.config;

import com.appointmentscheduler.backend.entity.*;
import com.appointmentscheduler.backend.enums.BayType;
import com.appointmentscheduler.backend.enums.RegionStrategy;
import com.appointmentscheduler.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final ServiceCenterRepository serviceCenterRepository;
    private final PartsInventoryRepository partsInventoryRepository;
    private final ServiceCatalogRepository serviceCatalogRepository;
    private final ServicePartsMappingRepository servicePartsMappingRepository;
    private final TechnicianRepository technicianRepository;
    private final ServiceBayRepository serviceBayRepository;
    private final CustomerRepository customerRepository;
    private final TechnicianSchedulerRepository technicianSchedulerRepository;
    private final BaySchedulerRepository baySchedulerRepository;

    private static final int TOTAL_SLOTS = 18; // 9 hours * 2 slots/hour
    private final Random random = new Random();

    @Override
    public void run(String... args) {
        // Check if data already exists
        if (serviceCenterRepository.count() > 0) {
            System.out.println("Data already seeded. Skipping...");
            return;
        }

        System.out.println("Starting data seeding...");

        // 1. Create Service Centers
        ServiceCenter texasCenter = ServiceCenter.builder()
            .name("Texas Auto Hub")
            .regionStrategy(RegionStrategy.PREDICTIVE_US)
            .build();

        ServiceCenter londonCenter = ServiceCenter.builder()
            .name("London Service Centre")
            .regionStrategy(RegionStrategy.DETERMINISTIC_EU)
            .build();

        texasCenter = serviceCenterRepository.save(texasCenter);
        londonCenter = serviceCenterRepository.save(londonCenter);

        System.out.println("Created service centers");

        // 2. Create Parts Inventory (10 parts for both centers)
        List<String> partNames = Arrays.asList(
    "Brake Pads", "Rotors", "OBDII Scanner", "Coolant", "Thermostat",
        "Engine Oil", "Oil Filter", "Battery", "New Tires", "Valve Stems"
        );

        List<Integer> quantities = Arrays.asList(
            1, 0, 1, 0, 1, 0, 0, 1, 0, 1
        );

        List<PartsInventory> texasParts = new ArrayList<>();
        List<PartsInventory> londonParts = new ArrayList<>();

        for (int i = 0; i < partNames.size(); i++) {
            String partName = partNames.get(i);
            int quantity = quantities.get(i);

            texasParts.add(PartsInventory.builder()
                .serviceCenter(texasCenter)
                .partName(partName)
                .availableParts(quantity)
                .orderedParts(0)
                .leadTimeDays(2)
                .build());

            londonParts.add(PartsInventory.builder()
                .serviceCenter(londonCenter)
                .partName(partName)
                .availableParts(quantity)
                .orderedParts(0)
                .leadTimeDays(2)
                .build());
        }

        partsInventoryRepository.saveAll(texasParts);
        partsInventoryRepository.saveAll(londonParts);

        System.out.println("Created parts inventory");

        // 3. Create Service Catalog (6 services)
        ServiceCatalog squeakingBrakes = ServiceCatalog.builder()
            .name("Squeaking Brakes")
            .durationMinutes(120)
            .requiredSkillLevel(2) // Level B
            .requiredBayType(BayType.GENERAL)
            .build();

        ServiceCatalog engineLight = ServiceCatalog.builder()
            .name("Engine Light")
            .durationMinutes(180)
            .requiredSkillLevel(1) // Level A
            .requiredBayType(BayType.GENERAL)
            .build();

        ServiceCatalog overheating = ServiceCatalog.builder()
            .name("Overheating")
            .durationMinutes(60)
            .requiredSkillLevel(2) // Level B
            .requiredBayType(BayType.GENERAL)
            .build();

        ServiceCatalog oilChange = ServiceCatalog.builder()
            .name("Oil Change")
            .durationMinutes(60)
            .requiredSkillLevel(3) // Level C
            .requiredBayType(BayType.QUICK)
            .build();

        ServiceCatalog deadBattery = ServiceCatalog.builder()
            .name("Dead Battery")
            .durationMinutes(60)
            .requiredSkillLevel(3) // Level C
            .requiredBayType(BayType.QUICK)
            .build();

        ServiceCatalog tireIssues = ServiceCatalog.builder()
            .name("Tire Issues")
            .durationMinutes(90)
            .requiredSkillLevel(2) // Level B
            .requiredBayType(BayType.TIRE_ISSUES)
            .build();

        squeakingBrakes = serviceCatalogRepository.save(squeakingBrakes);
        engineLight = serviceCatalogRepository.save(engineLight);
        overheating = serviceCatalogRepository.save(overheating);
        oilChange = serviceCatalogRepository.save(oilChange);
        deadBattery = serviceCatalogRepository.save(deadBattery);
        tireIssues = serviceCatalogRepository.save(tireIssues);

        System.out.println("Created service catalog");

        // 4. Create ServicePartsMapping
        // Get parts from Texas center (we'll use these for mappings)
        PartsInventory brakePads = partsInventoryRepository
            .findByServiceCenter_CenterIdAndPartName(texasCenter.getCenterId(), "Brake Pads").orElseThrow();
        PartsInventory rotors = partsInventoryRepository
            .findByServiceCenter_CenterIdAndPartName(texasCenter.getCenterId(), "Rotors").orElseThrow();
        PartsInventory obdScanner = partsInventoryRepository
            .findByServiceCenter_CenterIdAndPartName(texasCenter.getCenterId(), "OBDII Scanner").orElseThrow();
        PartsInventory coolant = partsInventoryRepository
            .findByServiceCenter_CenterIdAndPartName(texasCenter.getCenterId(), "Coolant").orElseThrow();
        PartsInventory thermostat = partsInventoryRepository
            .findByServiceCenter_CenterIdAndPartName(texasCenter.getCenterId(), "Thermostat").orElseThrow();
        PartsInventory engineOil = partsInventoryRepository
            .findByServiceCenter_CenterIdAndPartName(texasCenter.getCenterId(), "Engine Oil").orElseThrow();
        PartsInventory oilFilter = partsInventoryRepository
            .findByServiceCenter_CenterIdAndPartName(texasCenter.getCenterId(), "Oil Filter").orElseThrow();
        PartsInventory battery = partsInventoryRepository
            .findByServiceCenter_CenterIdAndPartName(texasCenter.getCenterId(), "Battery").orElseThrow();
        PartsInventory newTires = partsInventoryRepository
            .findByServiceCenter_CenterIdAndPartName(texasCenter.getCenterId(), "New Tires").orElseThrow();
        PartsInventory valveStems = partsInventoryRepository
            .findByServiceCenter_CenterIdAndPartName(texasCenter.getCenterId(), "Valve Stems").orElseThrow();

        List<ServicePartsMapping> mappings = Arrays.asList(
            // Squeaking Brakes: Brake Pads, Rotors
            ServicePartsMapping.builder()
                .serviceCatalog(squeakingBrakes)
                .partsInventory(brakePads)
                .quantityRequired(1)
                .build(),
            ServicePartsMapping.builder()
                .serviceCatalog(squeakingBrakes)
                .partsInventory(rotors)
                .quantityRequired(1)
                .build(),
            // Engine Light: OBDII Scanner
            ServicePartsMapping.builder()
                .serviceCatalog(engineLight)
                .partsInventory(obdScanner)
                .quantityRequired(1)
                .build(),
            // Overheating: Coolant, Thermostat
            ServicePartsMapping.builder()
                .serviceCatalog(overheating)
                .partsInventory(coolant)
                .quantityRequired(1)
                .build(),
            ServicePartsMapping.builder()
                .serviceCatalog(overheating)
                .partsInventory(thermostat)
                .quantityRequired(1)
                .build(),
            // Oil Change: Engine Oil, Oil Filter
            ServicePartsMapping.builder()
                .serviceCatalog(oilChange)
                .partsInventory(engineOil)
                .quantityRequired(1)
                .build(),
            ServicePartsMapping.builder()
                .serviceCatalog(oilChange)
                .partsInventory(oilFilter)
                .quantityRequired(1)
                .build(),
            // Dead Battery: Battery
            ServicePartsMapping.builder()
                .serviceCatalog(deadBattery)
                .partsInventory(battery)
                .quantityRequired(1)
                .build(),
            // Tire Issues: New Tires, Valve Stems
            ServicePartsMapping.builder()
                .serviceCatalog(tireIssues)
                .partsInventory(newTires)
                .quantityRequired(1)
                .build(),
            ServicePartsMapping.builder()
                .serviceCatalog(tireIssues)
                .partsInventory(valveStems)
                .quantityRequired(1)
                .build()
        );

        servicePartsMappingRepository.saveAll(mappings);

        System.out.println("Created service parts mappings");

        // 5. Create Technicians (6 total)
        List<Technician> technicians = new ArrayList<>();
        
        // Level A (skillLevel=1): Tech A1, Tech A2
        technicians.add(Technician.builder()
            .name("Tech A1")
            .skillLevel(1)
            .serviceCenter(texasCenter)
            .build());
        technicians.add(Technician.builder()
            .name("Tech A2")
            .skillLevel(1)
            .serviceCenter(londonCenter)
            .build());

        // Level B (skillLevel=2): Tech B1, Tech B2
        technicians.add(Technician.builder()
            .name("Tech B1")
            .skillLevel(2)
            .serviceCenter(texasCenter)
            .build());
        technicians.add(Technician.builder()
            .name("Tech B2")
            .skillLevel(2)
            .serviceCenter(londonCenter)
            .build());

        // Level C (skillLevel=3): Tech C1, Tech C2
        technicians.add(Technician.builder()
            .name("Tech C1")
            .skillLevel(3)
            .serviceCenter(texasCenter)
            .build());
        technicians.add(Technician.builder()
            .name("Tech C2")
            .skillLevel(3)
            .serviceCenter(londonCenter)
            .build());

        technicians = technicianRepository.saveAll(technicians);

        System.out.println("Created technicians");

        // 6. Create Service Bays (6 total, linked to Texas)
        List<ServiceBay> bays = Arrays.asList(
            ServiceBay.builder()
                .name("General Bay 1")
                .type(BayType.GENERAL)
                .serviceCenter(texasCenter)
                .build(),
            ServiceBay.builder()
                .name("General Bay 2")
                .type(BayType.GENERAL)
                .serviceCenter(londonCenter)
                .build(),
            ServiceBay.builder()
                .name("Quick Bay 1")
                .type(BayType.QUICK)
                .serviceCenter(texasCenter)
                .build(),
            ServiceBay.builder()
                .name("Quick Bay 2")
                .type(BayType.QUICK)
                .serviceCenter(londonCenter)
                .build(),
            ServiceBay.builder()
                .name("Tire Bay 1")
                .type(BayType.TIRE_ISSUES)
                .serviceCenter(texasCenter)
                .build(),
            ServiceBay.builder()
                .name("Tire Bay 2")
                .type(BayType.TIRE_ISSUES)
                .serviceCenter(londonCenter)
                .build()
        );

        bays = serviceBayRepository.saveAll(bays);

        System.out.println("Created service bays");

        // 7. Create Customers (10) with vehicles
        List<String> firstNames = Arrays.asList(
            "Alice", "Bob", "Charlie", "Diana", "Ethan",
            "Fiona", "George", "Hannah", "Ian", "Julia"
        );

        List<String> lastNames = Arrays.asList(
            "Smith", "Johnson", "Williams", "Brown", "Jones",
            "Garcia", "Miller", "Davis", "Rodriguez", "Wilson"
        );

        List<Customer> customers = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            String name = firstNames.get(i) + " " + lastNames.get(9 - i);
            String phone = String.format("555-01%02d", i);
            int loyaltyScore = 10 + random.nextInt(91); // 10-100

            customers.add(Customer.builder()
                .name(name)
                .phone(phone)
                .loyaltyScore(loyaltyScore)
                .build());
        }

        customers = customerRepository.saveAll(customers);

        System.out.println("Created customers");

        // Create vehicles so that every customer has at least one vehicle
        List<String> models = Arrays.asList(
            "Honda Civic", "Toyota Corolla", "Tesla Model 3", "Ford Focus",
            "Chevy Malibu", "BMW 3 Series", "Audi A4", "Nissan Altima",
            "Kia Optima", "Hyundai Elantra"
        );

        int vinCounter = 100000; // simple incremental VIN suffix

        for (int i = 0; i < customers.size(); i++) {
            Customer customer = customers.get(i);

            // Each customer gets at least one vehicle, some get two
            int vehiclesForCustomer = (i % 3 == 0) ? 2 : 1;

            customer.setVehicles(new ArrayList<>());

            for (int v = 0; v < vehiclesForCustomer; v++) {
                String vin = String.format("VIN%06d", vinCounter++);
                String model = models.get((i + v) % models.size());

                Vehicle vehicle = Vehicle.builder()
                    .vin(vin)
                    .model(model)
                    .customer(customer)
                    .build();

                customer.getVehicles().add(vehicle);
            }
        }

        customerRepository.saveAll(customers);
        System.out.println("Created vehicles for customers");

        // 8. Initialize Bitmask Schedules
        LocalDate today = LocalDate.now();
        List<TechnicianScheduler> techSchedules = new ArrayList<>();
        List<BayScheduler> baySchedules = new ArrayList<>();

        // Technician schedules: 6 techs × 30 days = 180 records
        for (Technician tech : technicians) {
            for (int dayOffset = 0; dayOffset < 30; dayOffset++) {
                techSchedules.add(TechnicianScheduler.builder()
                    .technician(tech)
                    .date(today.plusDays(dayOffset))
                    .bitmask(generateRandomBitmask())
                    .build());
            }
        }

        // Bay schedules: 6 bays × 30 days = 180 records
        for (ServiceBay bay : bays) {
            for (int dayOffset = 0; dayOffset < 30; dayOffset++) {
                baySchedules.add(BayScheduler.builder()
                    .bay(bay)
                    .date(today.plusDays(dayOffset))
                    .bitmask(generateRandomBitmask())
                    .build());
            }
        }

        technicianSchedulerRepository.saveAll(techSchedules);
        baySchedulerRepository.saveAll(baySchedules);

        System.out.println("Created bitmask schedules");
        System.out.println("Data seeding completed!");
    }

    /**
     * Generate a random availability bitmask for a single day.
     * 1 bit per 30-minute slot, limited to the first TOTAL_SLOTS bits.
     */
    
    private int generateRandomBitmask() {
        int mask = 0;
        int[] sizes = {2, 4, 6};

        while (random.nextBoolean()) {
            int size = sizes[random.nextInt(sizes.length)];
            int start = random.nextInt(TOTAL_SLOTS);
            if (start + size > TOTAL_SLOTS) continue;

            int block = ((1 << size) - 1) << start;
            if ((mask & block) != 0) continue;

            mask |= block;
        }

        return mask;
    }

}

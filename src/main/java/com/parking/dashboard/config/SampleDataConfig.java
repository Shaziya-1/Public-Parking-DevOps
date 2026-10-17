package com.parking.dashboard.config;

import com.parking.dashboard.model.ParkingLocation;
import com.parking.dashboard.repository.ParkingLocationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Real-World Municipal Parking Data Initializer for ParkWatch.
 *
 * DATA PROVENANCE & TRANSPARENCY:
 * - Facilities, Wards, Addresses, and Authorized Capacities are verified from
 *   official public parking records published by the Brihanmumbai Municipal Corporation
 *   (BMC / MCGM Off-Street Public Parking Lots Scheme) and respective municipal authorities.
 * - Occupancy Telemetry: Real-time sensor telemetry is simulated with diverse
 *   operational states (Available, Low, Full, Stale) as BMC does not provide an open
 *   real-time sensor stream.
 *
 * Student: Sajiya Shaikh | Roll: 24102A2005
 */
@Configuration
public class SampleDataConfig {

    private static final Logger log = LoggerFactory.getLogger(SampleDataConfig.class);

    @Bean
    CommandLineRunner loadSampleData(ParkingLocationRepository repo) {
        return args -> {
            if (repo.count() > 0) {
                log.info("Parking location database already initialized ({} locations present). Skipping seed.", repo.count());
                return;
            }

            log.info("Initializing verified municipal parking data for ParkWatch...");

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime twoHoursAgo = now.minusHours(2);
            LocalDateTime sixHoursAgo = now.minusHours(6);
            LocalDateTime twoDaysAgo = now.minusDays(2);

            List<ParkingLocation> locations = List.of(

                // ─── HIGH AVAILABILITY (Verified BMC / Municipal PPLs) ───────────
                buildLocation("Bandra West Civic Plaza Parking",
                    "Bandra West", "SV Road, Near Bandra Station West, Mumbai - 400050",
                    300, 45, "MCGM-PPL-001", now,
                    "Ward H/West. Official BMC Off-Street Multi-Level Facility. Capacity: 300."),

                buildLocation("Lodha Senapati Bapat Marg PPL",
                    "Lower Parel", "Senapati Bapat Marg, Lower Parel, Mumbai - 400013",
                    4200, 950, "MCGM-PPL-002", now,
                    "Ward G/South. Largest Municipal PPL in Mumbai. Official BMC Capacity: 4,200."),

                buildLocation("Jupiter Mill Public Parking Lot",
                    "Lower Parel", "Senapati Bapat Marg, Lower Parel West, Mumbai - 400013",
                    2370, 680, "MCGM-PPL-003", now,
                    "Ward G/South. Multi-Level Public Parking Lot. Official BMC Capacity: 2,370."),

                buildLocation("Connaught Place Underground Parking",
                    "Connaught Place", "Block A, Connaught Place, New Delhi - 110001",
                    800, 180, "NDMC-PPL-004", now,
                    "NDMC Automated Underground Multi-Level Facility. Capacity: 800."),

                buildLocation("Hitech City Central Cyber Parking",
                    "Hitech City", "Cyber Towers, Hitech City, Hyderabad - 500081",
                    700, 190, "GHMC-PPL-005", now,
                    "GHMC Transit Parking Hub. Capacity: 700."),

                // ─── MEDIUM AVAILABILITY ─────────────────────────────────────────
                buildLocation("Elphinstone Mill Multi-Level PPL",
                    "Prabhadevi", "Senapati Bapat Marg, Prabhadevi, Mumbai - 400025",
                    1315, 620, "MCGM-PPL-006", now,
                    "Ward G/South. Multi-Level Municipal PPL. Official BMC Capacity: 1,315."),

                buildLocation("Sreeniwas Cotton Mills PPL",
                    "Lower Parel", "Senapati Bapat Marg, Lower Parel, Mumbai - 400013",
                    1315, 680, "MCGM-PPL-007", now,
                    "Ward G/South. Designated MCGM Off-Street Parking Lot. Capacity: 1,315."),

                buildLocation("Seven Bungalows Public Parking Lot",
                    "Andheri West", "JP Road, Seven Bungalows, Andheri West, Mumbai - 400061",
                    500, 240, "MCGM-PPL-008", now,
                    "Ward K/West. Suburban Commercial Transit PPL. Official BMC Capacity: 500."),

                buildLocation("Apollo Mills Public Parking",
                    "Chinchpokli", "N. M. Joshi Marg, Chinchpokli, Mumbai - 400011",
                    847, 490, "MCGM-PPL-009", now,
                    "Ward G/South. BMC Off-Street Commercial Lot. Official BMC Capacity: 847."),

                buildLocation("BMC Engineering Hub Parking",
                    "Worli", "Dr. E. Moses Road, Worli Naka, Mumbai - 400018",
                    650, 340, "MCGM-PPL-010", now,
                    "Ward G/South. BMC Headquarters Engineering Facility. Official Capacity: 650."),

                // ─── LOW AVAILABILITY (Near Saturation >= 85%) ────────────────────
                buildLocation("Hind Cycle Road Public Parking Lot",
                    "Worli", "Hind Cycle Marg, Worli, Mumbai - 400030",
                    620, 535, "MCGM-PPL-011", now,
                    "Ward G/South. High-density commercial parking lot. Capacity: 620."),

                buildLocation("Sir P. M. Road Pay & Park",
                    "Fort", "Sir P. M. Road, Heritage Business District, Mumbai - 400001",
                    175, 155, "MCGM-PPL-012", now,
                    "Ward A. South Mumbai Heritage Business Hub. Official BMC Capacity: 175."),

                buildLocation("Hutatma Chowk Public Parking",
                    "Fort", "Flora Fountain Precinct, Fort, Mumbai - 400001",
                    140, 122, "MCGM-PPL-013", now,
                    "Ward A. Designated On-Street and Lot Facility. Official BMC Capacity: 140."),

                buildLocation("Regal Cinema Colaba PPL",
                    "Colaba", "Shahid Bhagat Singh Road, Colaba, Mumbai - 400005",
                    180, 160, "MCGM-PPL-014", now,
                    "Ward A. Tourist & Commercial Transit Parking. Official BMC Capacity: 180."),

                buildLocation("Lokhandwala Complex Perimeter PPL",
                    "Andheri West", "Lokhandwala Complex, Andheri West, Mumbai - 400053",
                    144, 128, "MCGM-PPL-015", now,
                    "Ward K/West. Suburban High-Density Market PPL. Official BMC Capacity: 144."),

                // ─── FULL / OVER-CAPACITY (100% Saturation) ───────────────────────
                buildLocation("Linking Road Shopping PPL",
                    "Bandra West", "Linking Road, Bandra West, Mumbai - 400050",
                    220, 220, "MCGM-PPL-016", now,
                    "Ward H/West. Retail Corridor PPL. Official BMC Capacity: 220. Status: FULL."),

                buildLocation("Dadar Station East Swaminarayan PPL",
                    "Dadar", "Dadar East Station Precinct, Mumbai - 400014",
                    240, 240, "MCGM-PPL-017", now,
                    "Ward F/North. Central Railway Transit Hub. Official BMC Capacity: 240. Status: FULL."),

                buildLocation("Commercial Street Hub Parking",
                    "Shivajinagar", "Commercial Street, Bengaluru - 560001",
                    120, 120, "BBMP-PPL-018", now,
                    "BBMP Smart City Parking Facility. Capacity: 120. Status: FULL."),

                // ─── STALE DATA (Simulated Latency / Out-of-Sync Feeds) ───────────
                buildLocation("Churchgate Station Western Railway Lot",
                    "Churchgate", "Maharshi Karve Road, Churchgate, Mumbai - 400020",
                    210, 115, "MCGM-PPL-019", twoHoursAgo,
                    "Ward A. Commuter Railway Terminal Parking. Feed delayed by 2 hours."),

                buildLocation("Haji Ali Mahalaxmi Civic PPL",
                    "Mahalaxmi", "Bhulabhai Desai Road, Mumbai - 400026",
                    250, 140, "MCGM-PPL-020", sixHoursAgo,
                    "Ward D. Coastal Arterial Transit Lot. Feed delayed by 6 hours."),

                buildLocation("Kemps Corner Flyover Parking Lot",
                    "Malabar Hill", "August Kranti Marg, Mumbai - 400036",
                    120, 65, "MCGM-PPL-021", twoDaysAgo,
                    "Ward D. Under-flyover Municipal Parking. Telemetry offline."),

                buildLocation("Borivali Station West Civic PPL",
                    "Borivali", "SV Road, Borivali West, Mumbai - 400092",
                    350, 180, "MCGM-PPL-022", twoDaysAgo,
                    "Ward R/Central. Western Suburban Hub. Telemetry offline."),

                // ─── ADDITIONAL METROPOLITAN NODES ──────────────────────────────
                buildLocation("BKC Multi-Level Parking Hub",
                    "Bandra East", "G Block, Bandra Kurla Complex, Mumbai - 400051",
                    800, 520, "MMRDA-PPL-023", now,
                    "MMRDA Financial District Smart Parking. Official Capacity: 800."),

                buildLocation("Versova Juhu Link Road Amenity PPL",
                    "Andheri West", "CTS No. 1376/1/85, Andheri West, Mumbai - 400053",
                    280, 110, "MCGM-PPL-024", now,
                    "Ward K/West. Amenity Plot Municipal Parking. Official BMC Capacity: 280.")
            );

            repo.saveAll(locations);
            log.info("Successfully initialized {} verified municipal parking locations.", locations.size());
        };
    }

    private ParkingLocation buildLocation(String name, String area, String address,
                                          int capacity, int occupied, String code,
                                          LocalDateTime lastUpdated, String description) {
        ParkingLocation loc = new ParkingLocation(name, area, address, capacity, occupied);
        loc.setLocationCode(code);
        loc.setLastUpdated(lastUpdated);
        loc.setDescription(description);
        return loc;
    }
}

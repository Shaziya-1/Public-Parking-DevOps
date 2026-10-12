package com.parking.dashboard.config;

import com.parking.dashboard.model.ParkingLocation;
import com.parking.dashboard.repository.ParkingLocationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Seed data configuration for the 'dev' and 'default' profiles.
 *
 * NOTE: All parking locations and occupancy values listed here are
 * ILLUSTRATIVE SAMPLE DATA created for academic demonstration purposes.
 * They do not represent real-time sensor readings or actual parking conditions.
 *
 * Provides 20+ sample locations across realistic Indian city locality names,
 * covering a range of availability conditions: high, medium, low, full, and stale.
 */
@Configuration
@Profile({"default", "dev"})
public class SampleDataConfig {

    private static final Logger log = LoggerFactory.getLogger(SampleDataConfig.class);

    @Bean
    CommandLineRunner loadSampleData(ParkingLocationRepository repo) {
        return args -> {
            if (repo.count() > 0) {
                log.info("Sample data already loaded ({} locations). Skipping.", repo.count());
                return;
            }

            log.info("Loading sample parking data for demonstration...");

            // Reference time for stale-data simulation
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime twoHoursAgo = now.minusHours(2);
            LocalDateTime sixHoursAgo = now.minusHours(6);
            LocalDateTime twoDaysAgo = now.minusDays(2);

            List<ParkingLocation> locations = List.of(

                // ─── HIGH AVAILABILITY ───────────────────────────────────────────
                buildLocation("Bandra West Civic Plaza Parking",
                    "Bandra West", "SV Road, Near Bandra Station, Mumbai - 400050",
                    300, 45, "PPL-001", now),

                buildLocation("Koramangala IT Park Parking",
                    "Koramangala", "80 Feet Road, Koramangala 4th Block, Bengaluru - 560034",
                    500, 120, "PPL-002", now),

                buildLocation("Sector 17 Government Parking",
                    "Sector 17", "Sector 17 Plaza, Chandigarh - 160017",
                    200, 30, "PPL-003", now),

                buildLocation("Connaught Place Underground Parking",
                    "Connaught Place", "Block A, Connaught Place, New Delhi - 110001",
                    800, 180, "PPL-004", now),

                buildLocation("Jubilee Hills Club Parking",
                    "Jubilee Hills", "Road No. 36, Jubilee Hills, Hyderabad - 500033",
                    250, 55, "PPL-005", now),

                // ─── MEDIUM AVAILABILITY ─────────────────────────────────────────
                buildLocation("Vashi Railway Station Parking",
                    "Vashi", "Sector 17, Vashi, Navi Mumbai - 400703",
                    450, 260, "PPL-006", now),

                buildLocation("MG Road Metro Parking",
                    "MG Road", "MG Road Metro Station, Bengaluru - 560001",
                    350, 195, "PPL-007", now),

                buildLocation("Salt Lake Sector V Parking",
                    "Salt Lake Sector V", "Webel More, Sector V, Kolkata - 700091",
                    400, 225, "PPL-008", now),

                buildLocation("Garware College Road Parking",
                    "Shivajinagar", "FC Road, Shivajinagar, Pune - 411005",
                    180, 100, "PPL-009", now),

                buildLocation("Anna Nagar Tower Parking",
                    "Anna Nagar", "3rd Avenue, Anna Nagar West, Chennai - 600040",
                    300, 165, "PPL-010", now),

                // ─── LOW AVAILABILITY ────────────────────────────────────────────
                buildLocation("Phoenix MarketCity Parking - Block C",
                    "Whitefield", "Whitefield Main Road, Bengaluru - 560066",
                    600, 540, "PPL-011", now),

                buildLocation("Linking Road Shopping Parking",
                    "Bandra West", "Linking Road, Bandra West, Mumbai - 400050",
                    220, 198, "PPL-012", now),

                buildLocation("Laxmi Nagar District Centre Parking",
                    "Laxmi Nagar", "Laxmi Nagar District Centre, New Delhi - 110092",
                    280, 255, "PPL-013", now),

                buildLocation("Hitech City Central Parking",
                    "Hitech City", "Cyber Towers, Hitech City, Hyderabad - 500081",
                    700, 630, "PPL-014", now),

                buildLocation("Law Garden Night Market Parking",
                    "Ellisbridge", "Law Garden Road, Ellisbridge, Ahmedabad - 380006",
                    150, 138, "PPL-015", now),

                // ─── FULL / OVER-CAPACITY ─────────────────────────────────────────
                buildLocation("Sarojini Nagar Market Parking",
                    "Sarojini Nagar", "Sarojini Nagar Market, New Delhi - 110023",
                    180, 180, "PPL-016", now),

                buildLocation("Dadar Station East Parking",
                    "Dadar", "Dadar East, Mumbai - 400014",
                    120, 120, "PPL-017", now),

                buildLocation("Commercial Street Parking",
                    "Commercial Street", "Commercial Street, Bengaluru - 560001",
                    90, 90, "PPL-018", now),

                // ─── STALE DATA (last updated many hours/days ago) ───────────────
                buildLocation("Churchgate Station Parking",
                    "Churchgate", "Marine Lines, Mumbai - 400020",
                    200, 95, "PPL-019", twoHoursAgo),

                buildLocation("Karol Bagh Multi-Level Parking",
                    "Karol Bagh", "Ajmal Khan Road, Karol Bagh, New Delhi - 110005",
                    400, 210, "PPL-020", sixHoursAgo),

                buildLocation("Egmore Bus Terminus Parking",
                    "Egmore", "EVR Periyar Salai, Egmore, Chennai - 600008",
                    300, 145, "PPL-021", twoDaysAgo),

                buildLocation("Majestic Bus Stand Parking",
                    "Majestic", "Kempegowda Bus Station, Bengaluru - 560009",
                    350, 175, "PPL-022", twoDaysAgo),

                // ─── ADDITIONAL VARIETY ──────────────────────────────────────────
                buildLocation("Sector 18 Noida Parking",
                    "Sector 18 Noida", "Sector 18, Noida - 201301",
                    500, 320, "PPL-023", now),

                buildLocation("Vastrapur Lake Parking",
                    "Vastrapur", "Vastrapur Lake Road, Ahmedabad - 380015",
                    120, 20, "PPL-024", now)
            );

            repo.saveAll(locations);
            log.info("Loaded {} sample parking locations.", locations.size());
        };
    }

    private ParkingLocation buildLocation(String name, String area, String address,
                                          int capacity, int occupied, String code,
                                          LocalDateTime lastUpdated) {
        ParkingLocation loc = new ParkingLocation(name, area, address, capacity, occupied);
        loc.setLocationCode(code);
        loc.setLastUpdated(lastUpdated);
        return loc;
    }
}

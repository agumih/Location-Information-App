package org.example;

import java.util.Map;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        LocationQuery loc = new LocationQuery(30336); // example ZIP
        try {
            DetailedLocationInfo info = loc.fetchData();

            // Example of how to access and print the data if needed outside JavaFX
            System.out.println("\n--- Location Information ---");
            System.out.printf("📍 %s, %s (ZIP %d)\n", info.getCity(), info.getState(), info.getZipCode());
            System.out.printf("Coordinates: %.6f, %.6f\n", info.getLatitude(), info.getLongitude());

            System.out.println("\n--- Housing Information ---");
            if (info.getHousingInfo().avgHomePrice != -1) {
                System.out.printf("Avg Home Price: $%.0f\n", info.getHousingInfo().avgHomePrice);
            } else {
                System.out.println("Average home price not available.");
            }
            if (info.getHousingInfo().avgRent != -1) {
                System.out.printf("Avg Rent: $%.0f\n", info.getHousingInfo().avgRent);
            } else {
                System.out.println("Average rent not available.");
            }

            System.out.println("\n--- Median Income ---");
            if (info.getMedianIncome() != -1) {
                System.out.printf("Median Income: $%d\n", info.getMedianIncome());
            } else {
                System.out.println("Median income not available.");
            }

            System.out.println("\n--- Local NCES Schools ---");
            List<LocalSchoolFinder.School> schools = info.getSchools();
            if (!schools.isEmpty()) {
                for (int i = 0; i < schools.size(); i++) {
                    System.out.printf("%d. %s%n", i + 1, schools.get(i));
                }
            } else {
                System.out.println("No schools found for this ZIP code.");
            }

            System.out.println("\n--- Demographic Snapshot ---");
            Map<String, String> demographics = info.getDemographics();
            CensusDemographicsFetcher.printDemographics(demographics); // Re-using existing print logic for console output

        } catch (Exception e) {
            System.err.println("Error fetching data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

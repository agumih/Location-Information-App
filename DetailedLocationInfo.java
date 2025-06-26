package org.example;

import java.util.List;
import java.util.Map;

/**
 * A data class to encapsulate all information retrieved for a given ZIP code,
 * including location, housing, schools, and demographics.
 * This object will be passed from the backend logic to the JavaFX UI for display.
 */
public class DetailedLocationInfo {
    private final int zipCode;
    private final String city;
    private final String state;
    private final double latitude;
    private final double longitude;
    private final HousingAPI.HousingInfo housingInfo;
    private final int medianIncome;
    private final List<LocalSchoolFinder.School> schools;
    private final Map<String, String> demographics;

    public DetailedLocationInfo(int zipCode, String city, String state, double latitude, double longitude,
                                HousingAPI.HousingInfo housingInfo, int medianIncome,
                                List<LocalSchoolFinder.School> schools, Map<String, String> demographics) {
        this.zipCode = zipCode;
        this.city = city;
        this.state = state;
        this.latitude = latitude;
        this.longitude = longitude;
        this.housingInfo = housingInfo;
        this.medianIncome = medianIncome;
        this.schools = schools;
        this.demographics = demographics;
    }

    // Getters
    public int getZipCode() { return zipCode; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public HousingAPI.HousingInfo getHousingInfo() { return housingInfo; }
    public int getMedianIncome() { return medianIncome; }
    public List<LocalSchoolFinder.School> getSchools() { return schools; }
    public Map<String, String> getDemographics() { return demographics; }

    /**
     * Helper method to calculate age group population.
     * This logic is duplicated from CensusDemographicsFetcher.printDemographics,
     * but made public here for direct use by the UI layer if it chooses to format.
     */
    public int getPopulationForAgeGroup(String groupName) {
        if (demographics == null || demographics.isEmpty()) {
            return 0;
        }

        switch (groupName) {
            case "under18":
                return parseIntOrDefault(demographics, "B01001_003E") +
                        parseIntOrDefault(demographics, "B01001_004E") +
                        parseIntOrDefault(demographics, "B01001_005E") +
                        parseIntOrDefault(demographics, "B01001_006E") +
                        parseIntOrDefault(demographics, "B01001_027E") +
                        parseIntOrDefault(demographics, "B01001_028E") +
                        parseIntOrDefault(demographics, "B01001_029E") +
                        parseIntOrDefault(demographics, "B01001_030E");
            case "age18to44":
                return parseIntOrDefault(demographics, "B01001_007E") +
                        parseIntOrDefault(demographics, "B01001_008E") +
                        parseIntOrDefault(demographics, "B01001_009E") +
                        parseIntOrDefault(demographics, "B01001_010E") +
                        parseIntOrDefault(demographics, "B01001_011E") +
                        parseIntOrDefault(demographics, "B01001_012E") +
                        parseIntOrDefault(demographics, "B01001_013E") +
                        parseIntOrDefault(demographics, "B01001_014E") +
                        parseIntOrDefault(demographics, "B01001_031E") +
                        parseIntOrDefault(demographics, "B01001_032E") +
                        parseIntOrDefault(demographics, "B01001_033E") +
                        parseIntOrDefault(demographics, "B01001_034E") +
                        parseIntOrDefault(demographics, "B01001_035E") +
                        parseIntOrDefault(demographics, "B01001_036E") +
                        parseIntOrDefault(demographics, "B01001_037E") +
                        parseIntOrDefault(demographics, "B01001_038E");
            case "age45to64":
                return parseIntOrDefault(demographics, "B01001_015E") +
                        parseIntOrDefault(demographics, "B01001_016E") +
                        parseIntOrDefault(demographics, "B01001_017E") +
                        parseIntOrDefault(demographics, "B01001_018E") +
                        parseIntOrDefault(demographics, "B01001_019E") +
                        parseIntOrDefault(demographics, "B01001_039E") +
                        parseIntOrDefault(demographics, "B01001_040E") +
                        parseIntOrDefault(demographics, "B01001_041E") +
                        parseIntOrDefault(demographics, "B01001_042E") +
                        parseIntOrDefault(demographics, "B01001_043E");
            case "age65plus":
                return parseIntOrDefault(demographics, "B01001_020E") +
                        parseIntOrDefault(demographics, "B01001_021E") +
                        parseIntOrDefault(demographics, "B01001_022E") +
                        parseIntOrDefault(demographics, "B01001_023E") +
                        parseIntOrDefault(demographics, "B01001_024E") +
                        parseIntOrDefault(demographics, "B01001_025E") +
                        parseIntOrDefault(demographics, "B01001_044E") +
                        parseIntOrDefault(demographics, "B01001_045E") +
                        parseIntOrDefault(demographics, "B01001_046E") +
                        parseIntOrDefault(demographics, "B01001_047E") +
                        parseIntOrDefault(demographics, "B01001_048E") +
                        parseIntOrDefault(demographics, "B01001_049E");
            default:
                return 0;
        }
    }

    // Helper method to safely parse an integer from the demographics map.
    // Returns 0 if the key is not found or the value is not a valid integer.
    private static int parseIntOrDefault(Map<String, String> data, String key) {
        try {
            String value = data.getOrDefault(key, "0");
            // Census API can return negative values for some estimates due to sampling error,
            // or "null" / "-" strings for suppressed data. Treat these as 0 or handle as needed.
            if ("null".equalsIgnoreCase(value) || "-".equals(value) || value.isEmpty()) {
                return 0;
            }
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            System.err.println("Warning: Could not parse integer for key: " + key + ". Value was: " + data.get(key) + ". Error: " + e.getMessage());
            return 0;
        }
    }
}





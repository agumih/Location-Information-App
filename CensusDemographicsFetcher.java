package org.example;

import com.google.gson.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class CensusDemographicsFetcher {

    /**
     * Fetches demographic data for a given ZIP code from the Census API.
     * This method makes multiple API calls to gather all required variables due to API limits.
     *
     * @param zipCode The ZIP code for which to fetch demographics.
     * @return A Map containing demographic variable codes as keys and their values as strings.
     */
    public static Map<String, String> fetchDemographics(int zipCode) {
        Map<String, String> demographics = new HashMap<>();
        try {
            // --- First API Call: General Demographics, Income, Education ---
            // These variables are chosen to get the overall population, racial breakdown, education, and income.
            String generalVariables = String.join(",",
                    "B01003_001E", // Total population
                    "B02001_002E", // White alone
                    "B02001_003E", // Black or African American alone
                    "B02001_004E", // American Indian/Alaska Native
                    "B02001_005E", // Asian
                    "B02001_006E", // Native Hawaiian/Pacific Islander
                    "B02001_007E", // Some other race alone
                    "B02001_008E", // Two or more races
                    "B15003_017E", // Bachelor's degree
                    "B15003_022E", // Doctorate degree
                    "B19013_001E"  // Median income
            );

            fetchDataFromCensusApi(zipCode, generalVariables, demographics);

            // --- Second API Call: Age Data ---
            // These variables are for a detailed age breakdown, combining male and female categories
            // to calculate custom age ranges (Under 18, 18-44, 45-64, 65+).
            String ageVariables = String.join(",",
                    "B01001_003E", // Male: Under 5 years
                    "B01001_004E", // Male: 5 to 9 years
                    "B01001_005E", // Male: 10 to 14 years
                    "B01001_006E", // Male: 15 to 17 years
                    "B01001_027E", // Female: Under 5 years
                    "B01001_028E", // Female: 5 to 9 years
                    "B01001_029E", // Female: 10 to 14 years
                    "B01001_030E", // Female: 15 to 17 years

                    "B01001_007E", // Male: 18 and 19 years
                    "B01001_008E", // Male: 20 years
                    "B01001_009E", // Male: 21 years
                    "B01001_010E", // Male: 22 to 24 years
                    "B01001_011E", // Male: 25 to 29 years
                    "B01001_012E", // Male: 30 to 34 years
                    "B01001_013E", // Male: 35 to 39 years
                    "B01001_014E", // Male: 40 to 44 years
                    "B01001_031E", // Female: 18 and 19 years
                    "B01001_032E", // Female: 20 years
                    "B01001_033E", // Female: 21 years
                    "B01001_034E", // Female: 22 to 24 years
                    "B01001_035E", // Female: 25 to 29 years
                    "B01001_036E", // Female: 30 to 34 years
                    "B01001_037E", // Female: 35 to 39 years
                    "B01001_038E", // Female: 40 to 44 years

                    "B01001_015E", // Male: 45 to 49 years
                    "B01001_016E", // Male: 50 to 54 years
                    "B01001_017E", // Male: 55 to 59 years
                    "B01001_018E", // Male: 60 and 61 years
                    "B01001_019E", // Male: 62 to 64 years
                    "B01001_039E", // Female: 45 to 49 years
                    "B01001_040E", // Female: 50 to 54 years
                    "B01001_041E", // Female: 55 to 59 years
                    "B01001_042E", // Female: 60 and 61 years
                    "B01001_043E", // Female: 62 to 64 years

                    "B01001_020E", // Male: 65 and 66 years
                    "B01001_021E", // Male: 67 to 69 years
                    "B01001_022E", // Male: 70 to 74 years
                    "B01001_023E", // Male: 75 to 79 years
                    "B01001_024E", // Male: 80 to 84 years
                    "B01001_025E", // Male: 85 years and over
                    "B01001_044E", // Female: 65 and 66 years
                    "B01001_045E", // Female: 67 to 69 years
                    "B01001_046E", // Female: 70 to 74 years
                    "B01001_047E", // Female: 75 to 79 years
                    "B01001_048E", // Female: 80 to 84 years
                    "B01001_049E"  // Female: 85 years and over
            );

            fetchDataFromCensusApi(zipCode, ageVariables, demographics);

        } catch (Exception e) {
            System.err.println("Error fetching demographics from Census API: " + e.getMessage());
            return new HashMap<>();
        }
        return demographics;
    }

    /**
     * Helper method to make an API call to the Census Bureau and populate a target map.
     *
     * @param zipCode The ZIP code for the query.
     * @param variables A comma-separated string of Census variables to fetch.
     * @param targetMap The map to populate with the fetched data.
     * @throws Exception if there's an issue with the HTTP connection or JSON parsing.
     */
    private static void fetchDataFromCensusApi(int zipCode, String variables, Map<String, String> targetMap) throws Exception {
        String urlStr = String.format(
                "https://api.census.gov/data/2021/acs/acs5?get=NAME,%s&for=zip%%20code%%20tabulation%%20area:%d",
                variables, zipCode);

        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new RuntimeException("CensusAPI error: HTTP " + responseCode + " for URL: " + urlStr);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        JsonArray root = new Gson().fromJson(reader, JsonArray.class);
        reader.close();

        if (root == null || root.size() < 2) {
            System.out.println("No data found for the given ZIP code or variables in this API call (Response size < 2). URL: " + urlStr);
            return;
        }

        JsonArray headers = root.get(0).getAsJsonArray();
        JsonArray values = root.get(1).getAsJsonArray();

        for (int i = 0; i < headers.size(); i++) {
            String header = headers.get(i).getAsString();
            String value = values.get(i).getAsString();
            targetMap.put(header, value);
        }
    }

    /**
     * Prints demographic data to the console in a formatted way.
     * This method is kept for console-based testing/debugging but should not be
     * directly called by the JavaFX UI, which will render the data programmatically.
     *
     * @param data A Map containing demographic variable codes and their values.
     */
    public static void printDemographics(Map<String, String> data) {
        try {
            int total = Integer.parseInt(data.getOrDefault("B01003_001E", "0"));
            int white = Integer.parseInt(data.getOrDefault("B02001_002E", "0"));
            int black = Integer.parseInt(data.getOrDefault("B02001_003E", "0"));
            int nativeAmerican = Integer.parseInt(data.getOrDefault("B02001_004E", "0"));
            int asian = Integer.parseInt(data.getOrDefault("B02001_005E", "0"));
            int pacificIslander = Integer.parseInt(data.getOrDefault("B02001_006E", "0"));
            int otherRace = Integer.parseInt(data.getOrDefault("B02001_007E", "0"));
            int multiracial = Integer.parseInt(data.getOrDefault("B02001_008E", "0"));

            // Calculate age brackets dynamically
            int under18 = parseIntOrDefault(data, "B01001_003E") +
                    parseIntOrDefault(data, "B01001_004E") +
                    parseIntOrDefault(data, "B01001_005E") +
                    parseIntOrDefault(data, "B01001_006E") +
                    parseIntOrDefault(data, "B01001_027E") +
                    parseIntOrDefault(data, "B01001_028E") +
                    parseIntOrDefault(data, "B01001_029E") +
                    parseIntOrDefault(data, "B01001_030E");

            int age18to44 = parseIntOrDefault(data, "B01001_007E") +
                    parseIntOrDefault(data, "B01001_008E") +
                    parseIntOrDefault(data, "B01001_009E") +
                    parseIntOrDefault(data, "B01001_010E") +
                    parseIntOrDefault(data, "B01001_011E") +
                    parseIntOrDefault(data, "B01001_012E") +
                    parseIntOrDefault(data, "B01001_013E") +
                    parseIntOrDefault(data, "B01001_014E") +
                    parseIntOrDefault(data, "B01001_031E") +
                    parseIntOrDefault(data, "B01001_032E") +
                    parseIntOrDefault(data, "B01001_033E") +
                    parseIntOrDefault(data, "B01001_034E") +
                    parseIntOrDefault(data, "B01001_035E") +
                    parseIntOrDefault(data, "B01001_036E") +
                    parseIntOrDefault(data, "B01001_037E") +
                    parseIntOrDefault(data, "B01001_038E");

            int age45to64 = parseIntOrDefault(data, "B01001_015E") +
                    parseIntOrDefault(data, "B01001_016E") +
                    parseIntOrDefault(data, "B01001_017E") +
                    parseIntOrDefault(data, "B01001_018E") +
                    parseIntOrDefault(data, "B01001_019E") +
                    parseIntOrDefault(data, "B01001_039E") +
                    parseIntOrDefault(data, "B01001_040E") +
                    parseIntOrDefault(data, "B01001_041E") +
                    parseIntOrDefault(data, "B01001_042E") +
                    parseIntOrDefault(data, "B01001_043E");

            int age65plus = parseIntOrDefault(data, "B01001_020E") +
                    parseIntOrDefault(data, "B01001_021E") +
                    parseIntOrDefault(data, "B01001_022E") +
                    parseIntOrDefault(data, "B01001_023E") +
                    parseIntOrDefault(data, "B01001_024E") +
                    parseIntOrDefault(data, "B01001_025E") +
                    parseIntOrDefault(data, "B01001_044E") +
                    parseIntOrDefault(data, "B01001_045E") +
                    parseIntOrDefault(data, "B01001_046E") +
                    parseIntOrDefault(data, "B01001_047E") +
                    parseIntOrDefault(data, "B01001_048E") +
                    parseIntOrDefault(data, "B01001_049E");


            System.out.println("\n📊 Demographic Snapshot:");
            System.out.printf("Total Population: %,d\n", total);
            if (total > 0) {
                System.out.printf("White: %,d (%.1f%%)\n", white, white * 100.0 / total);
                System.out.printf("Black: %,d (%.1f%%)\n", black, black * 100.0 / total);
                System.out.printf("American Indian/Alaska Native: %,d (%.1f%%)\n", nativeAmerican, nativeAmerican * 100.0 / total);
                System.out.printf("Asian: %,d (%.1f%%)\n", asian, asian * 100.0 / total);
                System.out.printf("Two or More Races: %,d (%.1f%%)\n", multiracial, multiracial * 100.0 / total);
                System.out.printf("Other/Latino: %,d (%.1f%%)\n", otherRace, otherRace * 100.0 / total);
                System.out.println();

                System.out.println("Degrees Breakdown:");
                System.out.printf("Bachelor's Degrees: %s\n", data.getOrDefault("B15003_017E", "N/A"));
                System.out.printf("Doctorate Degrees: %s\n", data.getOrDefault("B15003_022E", "N/A"));
                System.out.printf("Median Income: $%s\n", data.getOrDefault("B19013_001E", "N/A"));

                System.out.println("\nAge Distribution:");
                System.out.printf("Under 18: %,d (%.1f%%)\n", under18, under18 * 100.0 / total);
                System.out.printf("18 to 44: %,d (%.1f%%)\n", age18to44, age18to44 * 100.0 / total);
                System.out.printf("45 to 64: %,d (%.1f%%)\n", age45to64, age45to64 * 100.0 / total);
                System.out.printf("65 and over: %,d (%.1f%%)\n", age65plus, age65plus * 100.0 / total);
            }
        } catch (Exception e) {
            System.err.println("❌ Error displaying demographics:");
            e.printStackTrace();
        }
    }

    /**
     * Helper method to safely parse an integer from the demographics map.
     * Returns 0 if the key is not found or the value is not a valid integer.
     */
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



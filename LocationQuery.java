package org.example;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class LocationQuery {
    private int zipCode;
    private String city;
    private String state;
    private double latitude, longitude;
    private int medianIncome = -1;

    // Zipcodebase API key. NOTE: This API Key is exposed in client-side code, for trial purposes only, it has a limited api calls
    private static final String ZIPCODEBASE_API_KEY = "e12d70c0-467c-11f0-b642-2b39e0ae98e0";

     private static final String APP_ID = "4fc35c9e";
     private static final String APP_KEY = "9454660d3e53c08410b85bbd0c226220";

    public LocationQuery(int zipCode) {
        this.zipCode = zipCode;
    }

    // Getters for JavaFX to access data
    public int getZipCode() { return zipCode; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public int getMedianIncome() { return medianIncome; }


    /**
     * Fetches all relevant location data (city, state, coordinates, housing, schools, demographics).
     * This method orchestrates calls to various APIs and local data sources.
     *
     * @return A DetailedLocationInfo object containing all fetched data.
     */
    public DetailedLocationInfo fetchData() {
        // Fetch basic location info (city, state, coords)
        fetchFromZipcodebase(zipCode);

        // Fetch housing info
        HousingAPI.HousingInfo housingInfo = HousingAPI.fetchHousingInfo(zipCode);

        // Fetch median income using Census API
        fetchMedianIncome(zipCode);

        // Fetch local school data
        List<LocalSchoolFinder.School> schools = LocalSchoolFinder.getSchoolsByZip(String.format("%05d", zipCode));

        // Fetch comprehensive demographic data
        Map<String, String> demographics = CensusDemographicsFetcher.fetchDemographics(zipCode);

        // Return a single object containing all aggregated data
        return new DetailedLocationInfo(
                zipCode, city, state, latitude, longitude,
                housingInfo, medianIncome, schools, demographics
        );
    }

    /**
     * Fetches city, state, latitude, and longitude for the given ZIP code using Zipcodebase API.
     * Sets the instance variables of this LocationQuery object.
     *
     * @param zip The ZIP code to query.
     */
    private void fetchFromZipcodebase(int zip) {
        try {
            String urlStr = String.format(
                    "https://app.zipcodebase.com/api/v1/search?apikey=%s&codes=%d&country=us",
                    ZIPCODEBASE_API_KEY, zip);

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                System.err.println("Zipcodebase API error: HTTP " + responseCode + " for ZIP: " + zip);
                return;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            Gson gson = new Gson();
            JsonObject root = gson.fromJson(response.toString(), JsonObject.class);
            JsonArray results = root.getAsJsonObject("results")
                    .getAsJsonArray(String.valueOf(zip));

            if (results != null && results.size() > 0) {
                JsonObject data = results.get(0).getAsJsonObject();
                this.city = data.get("city").getAsString();
                this.state = data.get("state_code").getAsString();
                this.latitude = data.get("latitude").getAsDouble();
                this.longitude = data.get("longitude").getAsDouble();

                // Console print for initial fetch (can be removed later)
                System.out.printf("✔ City Fetched: %s, %s (%.6f, %.6f)%n", city, state, latitude, longitude);
            } else {
                System.err.println("No location data found for ZIP: " + zip + " from Zipcodebase.");
            }

        } catch (Exception e) {
            System.err.println("Error fetching from Zipcodebase for ZIP " + zip + ": " + e.getMessage());
        }
    }

    /**
     * Fetches median household income for the given ZIP code using the Census API.
     * Sets the `medianIncome` instance variable.
     *
     * @param zip The ZIP code to query.
     */
    private void fetchMedianIncome(int zip) {
        try {
            String apiUrl = String.format(
                    "https://api.census.gov/data/2021/acs/acs5?get=NAME,B19013_001E&for=zip+code+tabulation+area:%d",
                    zip
            );
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                System.err.println("CensusAPI (Median Income) error: HTTP " + responseCode + " for ZIP: " + zip);
                return;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            JsonArray root = new Gson().fromJson(reader, JsonArray.class);
            reader.close();

            if (root != null && root.size() >= 2) {
                JsonArray headers = root.get(0).getAsJsonArray();
                JsonArray values = root.get(1).getAsJsonArray();

                int idxIncome = -1;
                for (int i = 0; i < headers.size(); i++) {
                    if ("B19013_001E".equals(headers.get(i).getAsString())) {
                        idxIncome = i;
                        break;
                    }
                }

                if (idxIncome >= 0 && idxIncome < values.size()) {
                    String val = values.get(idxIncome).getAsString();
                    if (!"null".equalsIgnoreCase(val) && !"-".equals(val)) {
                        medianIncome = Integer.parseInt(val);
                        System.out.println("Median Household Income: $" + medianIncome); // Console print
                    } else {
                        System.err.println("Median income data is 'null' or '-' for ZIP: " + zip);
                        medianIncome = -1;
                    }
                } else {
                    System.err.println("Median income variable not found in Census API response for ZIP: " + zip);
                }
            } else {
                System.err.println("Empty or invalid JSON response for median income from Census API for ZIP: " + zip);
            }

        } catch (Exception e) {
            System.err.println("Error fetching median income from Census API for ZIP " + zip + ": " + e.getMessage());
            medianIncome = -1;
        }
    }
}







////    public String toString() {
////        return "📍 Location Information:\n" +
////                "City: " + city + "\n" +
////                "State: " + state + "\n" +
////                "ZIP: " + zipCode + "\n" +
////                "Coordinates: " + latitude + ", " + longitude + "\n";
////    }
//}

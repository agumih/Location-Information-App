package org.example;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HousingAPI {
    // NOTE: This API Key is purposely exposed in client-side code, only for trial runs, the key has limited api calls
    private static final String API_KEY = "4c908c4f075a4f7895e151228ff95170";/////////////

    public static HousingInfo fetchHousingInfo(int zip) {
        HousingInfo info = new HousingInfo();
        try {
            String apiUrl = String.format(
                    "https://api.rentcast.io/v1/markets?zipCode=%d",
                    zip
            );

            HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("X-Api-Key", API_KEY);///////////////////

            int code = conn.getResponseCode();
            if (code != 200) {
                System.out.println("HousingAPI error: HTTP " + code);
                return info;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            JsonObject root = new Gson().fromJson(reader, JsonObject.class);
            reader.close();


            JsonObject sale = root.has("saleData") ? root.getAsJsonObject("saleData") : null;
            JsonObject rental = root.has("rentalData") ? root.getAsJsonObject("rentalData") : null;

            if (sale != null && sale.has("averagePrice")) {
                info.avgHomePrice = sale.get("averagePrice").getAsDouble();
            }
            if (rental != null && rental.has("averageRent")) {
                info.avgRent = rental.get("averageRent").getAsDouble();
            }

        } catch (Exception e) {
            System.out.println("Error in HousingAPI.fetchHousingInfo:");
            e.printStackTrace();
        }
        return info;
    }

    public static class HousingInfo {
        public double avgHomePrice = -1;
        public double avgRent = -1;
    }
}




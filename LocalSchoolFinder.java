package org.example;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LocalSchoolFinder {

    public static class School {
        public String name;
        public String city;
        public String state;
        public String level; // e.g., "High", "Middle", "Elementary"
        public String zip;
        public String street;
        public String phone;
        public String isCharter; // "YES" or "NO"
        public String schoolType; // e.g., "Regular school", "Special education school"
        public String website;

        public School(String name, String city, String state, String level, String zip,
                      String street, String phone, String isCharter, String schoolType, String website) {
            this.name = name;
            this.city = city;
            this.state = state;
            this.level = level;
            this.zip = zip;
            this.street = street;
            this.phone = phone;
            this.isCharter = isCharter;
            this.schoolType = schoolType;
            this.website = website;
        }

        @Override
        public String toString() {
            return String.format("%s (%s) – %s, %s", name, level, city, state);
        }
    }

    /**
     * Retrieves a list of schools for a given ZIP code from a CSV file.
     * The CSV file `ccd_sch_029_2324_w_1a_073124.csv` must be in the resources folder.
     *
     * @param zip The 5-digit ZIP code as a string (e.g., "03036").
     * @return A List of School objects found in the given ZIP code, limited to 5.
     */
    public static List<School> getSchoolsByZip(String zip) {
        List<School> schools = new ArrayList<>();
        try (InputStream is = Objects.requireNonNull(
                LocalSchoolFinder.class.getClassLoader()
                        .getResourceAsStream("ccd_sch_029_2324_w_1a_073124.csv"),
                "School CSV file not found in resources."
        );
             CSVReader reader = new CSVReader(new InputStreamReader(is))) {

            String[] row;
            String[] headers = reader.readNext(); // Read header row

            // Find column indices
            int nameIdx = -1, cityIdx = -1, stateIdx = -1, zipIdx = -1, levelIdx = -1;
            int mStreet1Idx = -1, lStreet1Idx = -1; // Specific for MSTREET1 and LSTREET1
            int phoneIdx = -1;
            int websiteIdx = -1; // Primary for WEBSITE
            int schWebsiteIdx = -1; // Fallback for School_Web_Site
            int charterTextIdx = -1;
            int schTypeIdx = -1;

            if (headers != null) {
                for (int i = 0; i < headers.length; i++) {
                    String header = headers[i].trim();
                    switch (header) {
                        case "SCH_NAME": nameIdx = i; break;
                        case "MCITY": cityIdx = i; break;
                        case "MSTATE": stateIdx = i; break;
                        case "MZIP": zipIdx = i; break;
                        case "LEVEL": levelIdx = i; break;
                        case "MSTREET1": mStreet1Idx = i; break; // Mailing Street Address 1
                        case "LSTREET1": lStreet1Idx = i; break; // Location Street Address 1
                        case "PHONE": phoneIdx = i; break;
                        case "WEBSITE": websiteIdx = i; break; // Preferred website column
                        case "School_Web_Site": schWebsiteIdx = i; break; // Fallback website column
                        case "CHARTER_TEXT": charterTextIdx = i; break;
                        case "SCH_TYPE_TEXT": schTypeIdx = i; break;
                    }
                }
            }

            // Basic validation for essential columns
            if (nameIdx == -1 || cityIdx == -1 || stateIdx == -1 || zipIdx == -1) {
                throw new RuntimeException("Required columns (SCH_NAME, MCITY, MSTATE, MZIP) not found in CSV.");
            }

            // Read data rows
            while ((row = reader.readNext()) != null) {
                // Ensure row has enough columns and matches the ZIP
                if (row.length > zipIdx && safeGetColumn(row, zipIdx).equals(zip)) {
                    String name = safeGetColumn(row, nameIdx);
                    String city = safeGetColumn(row, cityIdx);
                    String state = safeGetColumn(row, stateIdx);
                    String level = safeGetColumn(row, levelIdx, "Unknown");

                    // Prioritize MSTREET1, then LSTREET1 for street address
                    String street = safeGetColumn(row, mStreet1Idx);
                    if (street.isEmpty() || street.equals("N/A")) {
                        street = safeGetColumn(row, lStreet1Idx, "N/A");
                    } else if (street.equals("-")) { // Handle specific "-" placeholder
                        street = safeGetColumn(row, lStreet1Idx, "N/A");
                    }

                    String phone = safeGetColumn(row, phoneIdx, "N/A");
                    String isCharter = safeGetColumn(row, charterTextIdx, "N/A");
                    String schoolType = safeGetColumn(row, schTypeIdx, "N/A");

                    // Prioritize WEBSITE, then School_Web_Site for website
                    String website = safeGetColumn(row, websiteIdx);
                    if (website.isEmpty() || website.equals("N/A")) {
                        website = safeGetColumn(row, schWebsiteIdx, "N/A");
                    } else if (website.equals("-")) { // Handle specific "-" placeholder
                        website = safeGetColumn(row, schWebsiteIdx, "N/A");
                    }

                    schools.add(new School(name, city, state, level, zip, street, phone, isCharter, schoolType, website));
                    if (schools.size() >= 5) break; // Limit to 5 schools
                }
            }
        } catch (CsvValidationException | IOException e) {
            System.err.println("❌ Error reading local school data CSV: " + e.getMessage());
        } catch (NullPointerException e) {
            System.err.println("❌ School CSV file 'ccd_sch_029_2324_w_1a_073124.csv' not found in resources. " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ General error in LocalSchoolFinder: " + e.getMessage());
            e.printStackTrace();
        }
        return schools;
    }

    // Helper to safely get column data, returning empty string if index out of bounds
    private static String safeGetColumn(String[] row, int index) {
        return (index != -1 && index < row.length) ? row[index].trim() : "";
    }

    // Helper to safely get column data with a default value if not found or empty
    private static String safeGetColumn(String[] row, int index, String defaultValue) {
        String value = safeGetColumn(row, index);
        return value.isEmpty() ? defaultValue : value;
    }
}






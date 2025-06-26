// LocationApp.java
package org.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocationApp extends Application {

    private TextField zipCodeInput;
    private Button fetchDataButton;
    private TextArea resultDisplay;
    private ProgressBar progressBar;
    private Label statusLabel;
    private TabPane tabPane;

    // Demographic Charts Tab
    private PieChart racePieChart;
    private PieChart agePieChart;

    // Schools Tab
    private VBox schoolsDisplayVBox;
    private StackPane rootStackPane; // Make StackPane accessible for popup management

    // Real Estate Tab
    private Label avgHomePriceLabel;
    private Label avgRentLabel;
    private Label medianIncomeLabel;

    // University Degrees Tab
    private BarChart<String, Number> degreeBarChart;
    private CategoryAxis xAxis;
    private NumberAxis yAxis;

    // ExecutorService for background tasks to keep UI responsive
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Location Information App");

        // --- Top Control Panel ---
        zipCodeInput = new TextField();
        zipCodeInput.setPromptText("Enter 5-digit ZIP code (e.g., 30336)");
        zipCodeInput.setMaxWidth(200);
        zipCodeInput.setAlignment(Pos.CENTER);
        zipCodeInput.setText("30336"); // Default ZIP for convenience

        fetchDataButton = new Button("Lookup");
        fetchDataButton.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        fetchDataButton.setPadding(new Insets(10, 20, 10, 20));

        progressBar = new ProgressBar(0);
        progressBar.setVisible(false);
        progressBar.setPrefWidth(200);

        statusLabel = new Label("Ready");
        statusLabel.setFont(Font.font("Arial", 12));
        statusLabel.setStyle("-fx-text-fill: #333;");

        VBox inputControls = new VBox(10, zipCodeInput, fetchDataButton, progressBar, statusLabel);
        inputControls.setAlignment(Pos.CENTER);
        inputControls.setPadding(new Insets(20));

        // --- TabPane for organized display ---
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Tab 1: Details
        Tab detailsTab = new Tab("Full Details");
        resultDisplay = new TextArea();
        resultDisplay.setEditable(false);
        resultDisplay.setWrapText(true);
        resultDisplay.setPrefRowCount(25);
        resultDisplay.setFont(Font.font("Monospaced", 13));
        VBox detailsContent = new VBox(10);
        detailsContent.setPadding(new Insets(10));
        detailsContent.getChildren().add(resultDisplay);
        detailsTab.setContent(detailsContent);

        // Tab 2: Demographics Charts (Race & Age Pie Charts)
        Tab demographicsChartsTab = new Tab("Demographics Charts");
        VBox demographicsChartsContent = new VBox(20);
        demographicsChartsContent.setAlignment(Pos.TOP_CENTER);
        demographicsChartsContent.setPadding(new Insets(20));

        racePieChart = new PieChart();
        racePieChart.setTitle("Racial Distribution");
        racePieChart.setLabelsVisible(true);
        racePieChart.setLabelLineLength(10);
        racePieChart.setLegendVisible(true);

        agePieChart = new PieChart();
        agePieChart.setTitle("Age Distribution");
        agePieChart.setLabelsVisible(true);
        agePieChart.setLabelLineLength(10);
        agePieChart.setLegendVisible(true);

        demographicsChartsContent.getChildren().addAll(racePieChart, agePieChart);
        demographicsChartsTab.setContent(demographicsChartsContent);

        // Tab 3: Schools
        Tab schoolsTab = new Tab("Schools");
        schoolsDisplayVBox = new VBox(15);
        schoolsDisplayVBox.setPadding(new Insets(20));
        schoolsDisplayVBox.setAlignment(Pos.TOP_LEFT);
        schoolsTab.setContent(new ScrollPane(schoolsDisplayVBox));

        // Tab 4: Real Estate & Income
        Tab realEstateTab = new Tab("Real Estate & Income");
        VBox realEstateContent = new VBox(15);
        realEstateContent.setPadding(new Insets(20));
        realEstateContent.setAlignment(Pos.TOP_LEFT);

        Label realEstateTitle = new Label("Housing & Income Data");
        realEstateTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        avgHomePriceLabel = new Label("Average Home Price: N/A");
        avgHomePriceLabel.setFont(Font.font("Arial", 14));
        avgRentLabel = new Label("Average Rent: N/A");
        avgRentLabel.setFont(Font.font("Arial", 14));
        medianIncomeLabel = new Label("Median Household Income: N/A");
        medianIncomeLabel.setFont(Font.font("Arial", 14));

        realEstateContent.getChildren().addAll(realEstateTitle, avgHomePriceLabel, avgRentLabel, medianIncomeLabel);
        realEstateTab.setContent(realEstateContent);

        // Tab 5: University Degrees
        Tab degreesTab = new Tab("University Degrees");
        VBox degreesContent = new VBox(20);
        degreesContent.setAlignment(Pos.TOP_CENTER);
        degreesContent.setPadding(new Insets(20));

        xAxis = new CategoryAxis();
        yAxis = new NumberAxis();
        degreeBarChart = new BarChart<>(xAxis, yAxis);
        degreeBarChart.setTitle("Highest Degree Attained");
        xAxis.setLabel("Degree Type");
        yAxis.setLabel("Population Estimate");
        degreeBarChart.setLegendVisible(false);
        degreeBarChart.setBarGap(5);
        degreeBarChart.setCategoryGap(20);

        degreeBarChart.setPrefWidth(350);
        degreeBarChart.setPrefHeight(400);
        degreeBarChart.setMaxWidth(350);
        degreeBarChart.setMaxHeight(400);

        degreesContent.getChildren().add(degreeBarChart);
        degreesTab.setContent(degreesContent);

        // Add all tabs to the TabPane
        tabPane.getTabs().addAll(detailsTab, demographicsChartsTab, schoolsTab, realEstateTab, degreesTab);

        // --- Main Layout Container (BorderPane inside StackPane) ---
        BorderPane mainLayout = new BorderPane();
        mainLayout.setTop(inputControls);
        mainLayout.setCenter(tabPane);
        BorderPane.setMargin(tabPane, new Insets(10, 20, 20, 20));

        // Set up the root StackPane
        rootStackPane = new StackPane(mainLayout); // mainLayout is the base layer
        rootStackPane.setPadding(new Insets(20)); // Overall padding for the app

        // Set up action for the button
        fetchDataButton.setOnAction(event -> fetchDataForZip());

        // --- Scene and Stage ---
        Scene scene = new Scene(rootStackPane, 1050, 900); // Scene uses the rootStackPane

        String css = ".axis-label {" +
                "    -fx-font-size: 14px;" +
                "    -fx-font-weight: bold;" +
                "    -fx-text-fill: #333;" +
                "}" +
                ".chart-bar {" +
                "    -fx-bar-fill: #8A2BE2;" + // purple color for now
                "}";
        scene.getStylesheets().add("data:text/css," + css);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Handles the data fetching process when the button is clicked.
     * Runs the backend logic on a separate thread and updates the UI on the JavaFX Application Thread.
     */
    private void fetchDataForZip() {
        String zipText = zipCodeInput.getText().trim();
        if (zipText.isEmpty()) {
            showAlert("Input Error", "Please enter a ZIP code.");
            return;
        }

        int zipCode;
        try {
            zipCode = Integer.parseInt(zipText);
            if (zipText.length() != 5) {
                throw new NumberFormatException("ZIP code must be 5 digits.");
            }
        } catch (NumberFormatException e) {
            showAlert("Input Error", "Invalid ZIP code. Please enter a 5-digit number.");
            return;
        }

        // Disable UI elements and show progress
        fetchDataButton.setDisable(true);
        zipCodeInput.setDisable(true);
        progressBar.setVisible(true);
        progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        statusLabel.setText("Fetching data for ZIP: " + zipCode + "...");
        clearAllDisplays(); // Clear all display areas before new data load

        final int finalZipCode = zipCode;

        // Create a Task to run the backend operations on a background thread
        Task<DetailedLocationInfo> fetchTask = new Task<DetailedLocationInfo>() {
            @Override
            protected DetailedLocationInfo call() throws Exception {
                // Instantiate LocationQuery and fetch data
                LocationQuery locationQuery = new LocationQuery(finalZipCode);
                return locationQuery.fetchData();
            }
        };

        // Set up event handlers for the task
        fetchTask.setOnSucceeded(e -> {
            DetailedLocationInfo info = fetchTask.getValue();
            // Update all display components
            displayDetails(info);
            updateDemographicCharts(info);
            displaySchools(info); // This will now create clickable cards
            displayRealEstate(info);
            updateDegreeChart(info);

            resetUI();
            statusLabel.setText("Data fetched successfully for the ZIP: " + finalZipCode);
        });

        fetchTask.setOnFailed(e -> {
            Throwable error = fetchTask.getException();
            System.err.println("Error during data fetch: " + error.getMessage());
            error.printStackTrace(); // Print full stack trace for debugging
            showAlert("Error", "Failed to fetch data: " + error.getMessage() + "\nPlease check your internet connection or the ZIP code.");
            resultDisplay.setText("Error: " + error.getMessage() + "\nSee console for details.");
            resetUI();
            statusLabel.setText("Error fetching data.");
        });

        fetchTask.setOnCancelled(e -> {
            showAlert("Operation Cancelled", "Data fetch was cancelled.");
            resetUI();
            statusLabel.setText("Operation cancelled.");
        });

        // Run the task on the executor service
        executorService.submit(fetchTask);
    }

    /**
     * Clears all display areas in preparation for new data.
     */
    private void clearAllDisplays() {
        resultDisplay.clear();
        racePieChart.setData(FXCollections.emptyObservableList());
        agePieChart.setData(FXCollections.emptyObservableList());
        schoolsDisplayVBox.getChildren().clear();
        avgHomePriceLabel.setText("Average Home Price: N/A");
        avgRentLabel.setText("Average Rent: N/A");
        medianIncomeLabel.setText("Median Household Income: N/A");
        degreeBarChart.setData(FXCollections.emptyObservableList()); // Clear bar chart
    }

    /**
     * Resets the UI elements to their initial state after a fetch operation.
     */
    private void resetUI() {
        fetchDataButton.setDisable(false);
        zipCodeInput.setDisable(false);
        progressBar.setVisible(false);
        progressBar.setProgress(0);
    }

    /**
     * Populates the 'Details' tab TextArea with comprehensive textual information.
     * @param info The DetailedLocationInfo object.
     */
    private void displayDetails(DetailedLocationInfo info) {
        StringBuilder sb = new StringBuilder();
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        NumberFormat percentFormat = NumberFormat.getPercentInstance(Locale.US);
        percentFormat.setMinimumFractionDigits(1);
        percentFormat.setMaximumFractionDigits(1);


        // --- Location Information ---
        sb.append("--- 📍 Location Information ---\n");
        if (info.getCity() != null && !info.getCity().isEmpty()) {
            sb.append(String.format("City, State: %s, %s (ZIP %d)\n", info.getCity(), info.getState(), info.getZipCode()));
            sb.append(String.format("Coordinates: %.6f, %.6f\n", info.getLatitude(), info.getLongitude()));
        } else {
            sb.append("Location data not available.\n");
        }


        // --- Housing Information --- (Duplicated here for comprehensive details tab)
        sb.append("\n--- 🏠 Housing Information ---\n");
        if (info.getHousingInfo().avgHomePrice != -1) {
            sb.append(String.format("Average Home Price: %s\n", currencyFormat.format(info.getHousingInfo().avgHomePrice)));
        } else {
            sb.append("Average home price not available.\n");
        }
        if (info.getHousingInfo().avgRent != -1) {
            sb.append(String.format("Average Rent: %s\n", currencyFormat.format(info.getHousingInfo().avgRent)));
        } else {
            sb.append("Average rent not available.\n");
        }


        // --- Median Income --- (Duplicated here for comprehensive details tab)
        sb.append("\n--- 💰 Median Income ---\n");
        if (info.getMedianIncome() != -1) {
            sb.append(String.format("Median Household Income: %s\n", currencyFormat.format(info.getMedianIncome())));
        } else {
            sb.append("Median household income not available.\n");
        }


        // --- Local NCES Schools --- (Duplicated here for comprehensive details tab)
        sb.append("\n--- 🏫 Local NCES Schools (Top 5) ---\n");
        List<LocalSchoolFinder.School> schools = info.getSchools();
        if (schools != null && !schools.isEmpty()) {
            for (int i = 0; i < schools.size(); i++) {
                sb.append(String.format("%d. %s\n", i + 1, schools.get(i).toString()));
            }
        } else {
            sb.append("No schools found for this ZIP code.\n");
        }


        // --- Demographic Snapshot (Textual) --- (Duplicated here for comprehensive details tab)
        sb.append("\n--- 📊 Demographic Snapshot ---\n");
        Map<String, String> demographics = info.getDemographics();
        if (demographics != null && !demographics.isEmpty()) {
            int total = Integer.parseInt(demographics.getOrDefault("B01003_001E", "0"));
            sb.append(String.format("Total Population: %,d\n", total));

            if (total > 0) {
                sb.append(String.format("White: %,d (%s)\n",
                        Integer.parseInt(demographics.getOrDefault("B02001_002E", "0")),
                        percentFormat.format(Integer.parseInt(demographics.getOrDefault("B02001_002E", "0")) * 1.0 / total)));
                sb.append(String.format("Black: %,d (%s)\n",
                        Integer.parseInt(demographics.getOrDefault("B02001_003E", "0")),
                        percentFormat.format(Integer.parseInt(demographics.getOrDefault("B02001_003E", "0")) * 1.0 / total)));
                sb.append(String.format("American Indian/Alaska Native: %,d (%s)\n",
                        Integer.parseInt(demographics.getOrDefault("B02001_004E", "0")),
                        percentFormat.format(Integer.parseInt(demographics.getOrDefault("B02001_004E", "0")) * 1.0 / total)));
                sb.append(String.format("Asian: %,d (%s)\n",
                        Integer.parseInt(demographics.getOrDefault("B02001_005E", "0")),
                        percentFormat.format(Integer.parseInt(demographics.getOrDefault("B02001_005E", "0")) * 1.0 / total)));
                sb.append(String.format("Native Hawaiian/Pacific Islander: %,d (%s)\n",
                        Integer.parseInt(demographics.getOrDefault("B02001_006E", "0")),
                        percentFormat.format(Integer.parseInt(demographics.getOrDefault("B02001_006E", "0")) * 1.0 / total)));
                sb.append(String.format("Two or More Races: %,d (%s)\n",
                        Integer.parseInt(demographics.getOrDefault("B02001_008E", "0")),
                        percentFormat.format(Integer.parseInt(demographics.getOrDefault("B02001_008E", "0")) * 1.0 / total)));
                sb.append(String.format("Other/Latino: %,d (%s)\n",
                        Integer.parseInt(demographics.getOrDefault("B02001_007E", "0")),
                        percentFormat.format(Integer.parseInt(demographics.getOrDefault("B02001_007E", "0")) * 1.0 / total)));

                sb.append("\n");
                sb.append("\nDegrees Breakdown:\n");
                sb.append(String.format("Bachelor's Degrees: %s\n", demographics.getOrDefault("B15003_017E", "N/A")));
                sb.append(String.format("Doctorate Degrees: %s\n", demographics.getOrDefault("B15003_022E", "N/A")));


                // Age Distribution
                sb.append("\nAge Distribution:\n");
                int under18 = info.getPopulationForAgeGroup("under18");
                int age18to44 = info.getPopulationForAgeGroup("age18to44");
                int age45to64 = info.getPopulationForAgeGroup("age45to64");
                int age65plus = info.getPopulationForAgeGroup("age65plus");

                if (total > 0) { // Ensure total population is not zero to avoid division by zero
                    sb.append(String.format("Under 18: %,d (%s)\n", under18, percentFormat.format(under18 * 1.0 / total)));
                    sb.append(String.format("18 to 44: %,d (%s)\n", age18to44, percentFormat.format(age18to44 * 1.0 / total)));
                    sb.append(String.format("45 to 64: %,d (%s)\n", age45to64, percentFormat.format(age45to64 * 1.0 / total)));
                    sb.append(String.format("65 and over: %,d (%s)\n", age65plus, percentFormat.format(age65plus * 1.0 / total)));
                } else {
                    sb.append("Age distribution not available due to zero total population.\n");
                }
            } else {
                sb.append("Demographic data not available or total population is zero.\n");
            }
        } else {
            sb.append("Demographic data not available.\n");
        }

        resultDisplay.setText(sb.toString());
    }

    /**
     * Updates the PieCharts with demographic data.
     * @param info The DetailedLocationInfo object containing all data.
     */
    private void updateDemographicCharts(DetailedLocationInfo info) {
        Map<String, String> demographics = info.getDemographics();
        if (demographics == null || demographics.isEmpty()) {
            racePieChart.setData(FXCollections.emptyObservableList());
            agePieChart.setData(FXCollections.emptyObservableList());
            racePieChart.setTitle("Racial Distribution (Data Not Available)");
            agePieChart.setTitle("Age Distribution (Data Not Available)");
            return;
        }

        int totalPopulation = Integer.parseInt(demographics.getOrDefault("B01003_001E", "0"));

        // --- Race Pie Chart Data ---
        ObservableList<PieChart.Data> racePieChartData = FXCollections.observableArrayList();
        if (totalPopulation > 0) {
            addPieData(racePieChartData, "White", Integer.parseInt(demographics.getOrDefault("B02001_002E", "0")), totalPopulation);
            addPieData(racePieChartData, "Black", Integer.parseInt(demographics.getOrDefault("B02001_003E", "0")), totalPopulation);
            addPieData(racePieChartData, "Am. Indian/Alaskan Native", Integer.parseInt(demographics.getOrDefault("B02001_004E", "0")), totalPopulation);
            addPieData(racePieChartData, "Asian", Integer.parseInt(demographics.getOrDefault("B02001_005E", "0")), totalPopulation);
            addPieData(racePieChartData, "Nat. Hawaiian/Pacific Islander", Integer.parseInt(demographics.getOrDefault("B02001_006E", "0")), totalPopulation);
            addPieData(racePieChartData, "Other/Latino", Integer.parseInt(demographics.getOrDefault("B02001_007E", "0")), totalPopulation); // Preserving user's label
            addPieData(racePieChartData, "Two or More Races", Integer.parseInt(demographics.getOrDefault("B02001_008E", "0")), totalPopulation);
        } else {
            racePieChartData.add(new PieChart.Data("No Data", 1));
        }
        racePieChart.setData(racePieChartData);
        racePieChart.setTitle("Racial Distribution");


        // --- Age Pie Chart Data ---
        ObservableList<PieChart.Data> agePieChartData = FXCollections.observableArrayList();
        if (totalPopulation > 0) {
            int under18 = info.getPopulationForAgeGroup("under18");
            int age18to44 = info.getPopulationForAgeGroup("age18to44");
            int age45to64 = info.getPopulationForAgeGroup("age45to64");
            int age65plus = info.getPopulationForAgeGroup("age65plus");

            addPieData(agePieChartData, "Under 18", under18, totalPopulation);
            addPieData(agePieChartData, "18 to 44", age18to44, totalPopulation);
            addPieData(agePieChartData, "45 to 64", age45to64, totalPopulation);
            addPieData(agePieChartData, "65 and over", age65plus, totalPopulation);
        } else {
            agePieChartData.add(new PieChart.Data("No Data", 1));
        }
        agePieChart.setData(agePieChartData);
        agePieChart.setTitle("Age Distribution");
    }

    /**
     * Displays the school information in the 'Schools' tab.
     * @param info The DetailedLocationInfo object.
     */
    private void displaySchools(DetailedLocationInfo info) {
        schoolsDisplayVBox.getChildren().clear(); // Clear previous school data

        List<LocalSchoolFinder.School> schools = info.getSchools();
        if (schools != null && !schools.isEmpty()) {
            Label header = new Label("Local NCES Schools (Top 5)");
            header.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            schoolsDisplayVBox.getChildren().add(header);

            for (LocalSchoolFinder.School school : schools) {
                VBox schoolCard = new VBox(5);
                schoolCard.setPadding(new Insets(10));
                schoolCard.setStyle("-fx-border-color: #ccc; -fx-border-radius: 5; -fx-background-color: #f9f9f9;");
                schoolCard.setCursor(javafx.scene.Cursor.HAND); // Indicate clickable

                Label nameLabel = new Label(school.name);
                nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                nameLabel.setWrapText(true);

                Label levelLabel = new Label("Level: " + school.level);
                Label locationLabel = new Label(school.city + ", " + school.state + " " + school.zip);

                schoolCard.getChildren().addAll(nameLabel, levelLabel, locationLabel);
                schoolsDisplayVBox.getChildren().add(schoolCard);

                // Add click event to show popup
                schoolCard.setOnMouseClicked(event -> showSchoolDetailsPopup(school));
            }
        } else {
            Label noSchoolsLabel = new Label("No schools found for this ZIP code.");
            noSchoolsLabel.setFont(Font.font("Arial", FontWeight.THIN, 14));
            schoolsDisplayVBox.getChildren().add(noSchoolsLabel);
        }
    }

    /**
     * Displays the real estate and income information in the 'Real Estate & Income' tab.
     * @param info The DetailedLocationInfo object.
     */
    private void displayRealEstate(DetailedLocationInfo info) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

        if (info.getHousingInfo().avgHomePrice != -1) {
            avgHomePriceLabel.setText(String.format("Average Home Price: %s", currencyFormat.format(info.getHousingInfo().avgHomePrice)));
        } else {
            avgHomePriceLabel.setText("Average Home Price: N/A");
        }

        if (info.getHousingInfo().avgRent != -1) {
            avgRentLabel.setText(String.format("Average Rent: %s", currencyFormat.format(info.getHousingInfo().avgRent)));
            avgRentLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        } else {
            avgRentLabel.setText("Average Rent: N/A");
            avgRentLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        }

        if (info.getMedianIncome() != -1) {
            medianIncomeLabel.setText(String.format("Median Household Income: %s", currencyFormat.format(info.getMedianIncome())));
        } else {
            medianIncomeLabel.setText("Median Household Income: N/A");
        }
    }

    /**
     * Updates the BarChart for university degrees.
     * @param info The DetailedLocationInfo object.
     */
    private void updateDegreeChart(DetailedLocationInfo info) {
        Map<String, String> demographics = info.getDemographics();
        degreeBarChart.setData(FXCollections.observableArrayList());

        if (demographics != null && !demographics.isEmpty()) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Degrees");

            int bachelors = parseIntOrDefault(demographics, "B15003_017E");
            int doctorates = parseIntOrDefault(demographics, "B15003_022E");

            if (bachelors > 0) {
                series.getData().add(new XYChart.Data<>("Bachelor's Degrees", bachelors));
            }
            if (doctorates > 0) {
                series.getData().add(new XYChart.Data<>("Doctorate Degrees", doctorates));
            }

            if (!series.getData().isEmpty()) {
                degreeBarChart.getData().add(series);
                degreeBarChart.setTitle("Highest Degree Attained");
            } else {
                degreeBarChart.setTitle("Highest Degree Attained (Data Not Available)");
            }
        } else {
            degreeBarChart.setTitle("Highest Degree Attained (Data Not Available)");
        }
    }

    /**
     * Helper method to show a detailed school information popup.
     * @param school The School object containing details to display.
     */
    private void showSchoolDetailsPopup(LocalSchoolFinder.School school) {
        VBox popupContent = new VBox(10); // Reduced spacing slightly for more content
        popupContent.setAlignment(Pos.CENTER_LEFT);
        popupContent.setPadding(new Insets(30));
        popupContent.setPrefSize(500, 350); // Slightly larger popup size
        popupContent.setMaxSize(500, 350); // Fixed size regardless of content
        popupContent.setStyle("-fx-background-color: white; -fx-border-color: #555; -fx-border-width: 2; -fx-border-radius: 10; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 10, 0, 0, 5);");

        Label nameLabel = new Label(school.name);
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20)); // Bigger font for name
        nameLabel.setWrapText(true);

        Label levelLabel = new Label("Level: " + school.level);
        levelLabel.setFont(Font.font("Arial", 15));

        // Updated for charter status
        Label charterLabel = new Label("Charter School: " + school.isCharter);
        charterLabel.setFont(Font.font("Arial", 15));

        // New for school type
        Label schoolTypeLabel = new Label("School Type: " + school.schoolType);
        schoolTypeLabel.setFont(Font.font("Arial", 15));

        // Correctly display street address. If 'street' is "N/A" or empty, just show city/state/zip.
        String fullAddress = school.street;
        if (!fullAddress.isEmpty() && !fullAddress.equals("N/A") && !fullAddress.equals("-")) { // Added check for "-"
            fullAddress += ", ";
        } else {
            fullAddress = ""; // Clear if "N/A" or empty or "-"
        }
        fullAddress += school.city + ", " + school.state + " " + school.zip;
        Label addressLabel = new Label("Address: " + fullAddress);
        addressLabel.setFont(Font.font("Arial", 15));
        addressLabel.setWrapText(true);

        Label phoneLabel = new Label("Phone: " + (school.phone != null && !school.phone.isEmpty() && !school.phone.equals("N/A") ? school.phone : "N/A"));
        phoneLabel.setFont(Font.font("Arial", 15));

        // Correctly display website. If 'website' is "N/A" or empty, just show N/A.
        Label websiteLabel = new Label("Website: " + (school.website != null && !school.website.isEmpty() && !school.website.equals("N/A") && !school.website.equals("-") ? school.website : "N/A")); // Added check for "-"
        websiteLabel.setFont(Font.font("Arial", 15));
        websiteLabel.setWrapText(true);

        // Update children to reflect new labels and order
        popupContent.getChildren().addAll(nameLabel, new Separator(), levelLabel, schoolTypeLabel, charterLabel, addressLabel, phoneLabel, websiteLabel);

        // Create a transparent overlay pane to capture clicks outside the popup
        Pane overlay = new Pane();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.4);"); // Semi-transparent black
        // Bind overlay size to rootStackPane size to ensure it covers the whole area
        overlay.prefWidthProperty().bind(rootStackPane.widthProperty());
        overlay.prefHeightProperty().bind(rootStackPane.heightProperty());
        overlay.setVisible(true); // Make visible when popup is shown

        // Add popup content and overlay to the main StackPane
        // Add overlay first, then popup so popup is visually on top
        rootStackPane.getChildren().addAll(overlay, popupContent);
        StackPane.setAlignment(popupContent, Pos.CENTER); // Center the popup in the StackPane

        // Event handler to dismiss popup when clicking anywhere on the overlay
        overlay.setOnMouseClicked(e -> {
            rootStackPane.getChildren().removeAll(overlay, popupContent); // Remove both
            e.consume(); // Consume event to prevent propagation to underlying nodes
        });
    }

    /**
     * Helper method to safely parse an integer from the demographics map.
     * Returns 0 if the key is not found or the value is not a valid integer.
     */
    private int parseIntOrDefault(Map<String, String> data, String key) {
        try {
            String value = data.getOrDefault(key, "0");
            if ("null".equalsIgnoreCase(value) || "-".equals(value) || value.isEmpty()) {
                return 0;
            }
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            System.err.println("Warning: Could not parse integer for key: " + key + ". Value was: " + data.get(key) + ". Error: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Helper method to add data to a PieChart.Data list.
     * Only adds if the value is greater than 0 to avoid cluttering the chart with zero-percentage slices.
     */
    private void addPieData(ObservableList<PieChart.Data> pieDataList, String name, int count, int total) {
        if (total > 0 && count > 0) {
            double percentage = (double) count / total;
            // Format for label: "Label (XX.X%)"
            String label = String.format("%s (%.1f%%)", name, percentage * 100);
            pieDataList.add(new PieChart.Data(label, count));
        }
    }

    /**
     * Shows a standard JavaFX alert dialog.
     * @param title The title of the alert.
     * @param message The message content of the alert.
     */
    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    @Override
    public void stop() {
        // Shutdown the executor service when the application closes
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}




//// LocationApp.java
//package org.example;
//
//import javafx.application.Application;
//import javafx.application.Platform;
//import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;
//import javafx.concurrent.Task;
//import javafx.geometry.Insets;
//import javafx.geometry.Pos;
//import javafx.scene.Scene;
//import javafx.scene.chart.BarChart; // Import BarChart
//import javafx.scene.chart.CategoryAxis; // Import CategoryAxis
//import javafx.scene.chart.NumberAxis;   // Import NumberAxis
//import javafx.scene.chart.PieChart;
//import javafx.scene.chart.XYChart; // Import XYChart for BarChart data
//import javafx.scene.control.*;
//import javafx.scene.layout.BorderPane;
//import javafx.scene.layout.GridPane;
//import javafx.scene.layout.HBox; // For horizontal layout in real estate tab
//import javafx.scene.layout.VBox;
//import javafx.scene.text.Font;
//import javafx.scene.text.FontWeight;
//import javafx.scene.text.Text;
//import javafx.stage.Stage;
//
//import java.text.NumberFormat;
//import java.util.List;
//import java.util.Locale;
//import java.util.Map;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//public class LocationApp extends Application {
//
//    private TextField zipCodeInput;
//    private Button fetchDataButton;
//    private TextArea resultDisplay; // Kept for 'Details' tab for comprehensive text
//    private ProgressBar progressBar;
//    private Label statusLabel;
//    private TabPane tabPane;
//
//    // Demographic Charts Tab
//    private PieChart racePieChart;
//    private PieChart agePieChart;
//
//    // Schools Tab
//    private VBox schoolsDisplayVBox; // Container for schools list
//
//    // Real Estate Tab
//    private Label avgHomePriceLabel;
//    private Label avgRentLabel;
//    private Label medianIncomeLabel;
//
//    // University Degrees Tab
//    private BarChart<String, Number> degreeBarChart; // New BarChart for degrees
//    private CategoryAxis xAxis;
//    private NumberAxis yAxis;
//
//
//    // ExecutorService for background tasks to keep UI responsive
//    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
//
//    @Override
//    public void start(Stage primaryStage) {
//        primaryStage.setTitle("Location Information App");
//
//        // --- Top Control Panel ---
//        zipCodeInput = new TextField();
//        zipCodeInput.setPromptText("Enter 5-digit ZIP code (e.g., 30336)");
//        zipCodeInput.setMaxWidth(200);
//        zipCodeInput.setAlignment(Pos.CENTER);
//        zipCodeInput.setText("30336"); // Default ZIP for convenience
//
//        fetchDataButton = new Button("Lookup");
//        fetchDataButton.setFont(Font.font("Arial", FontWeight.BOLD, 14));
//        fetchDataButton.setPadding(new Insets(10, 20, 10, 20));
//
//        progressBar = new ProgressBar(0);
//        progressBar.setVisible(false); // Hidden initially
//        progressBar.setPrefWidth(200);
//
//        statusLabel = new Label("Ready");
//        statusLabel.setFont(Font.font("Arial", 12));
//        statusLabel.setStyle("-fx-text-fill: #333;");
//
//        VBox inputControls = new VBox(10, zipCodeInput, fetchDataButton, progressBar, statusLabel);
//        inputControls.setAlignment(Pos.CENTER);
//        inputControls.setPadding(new Insets(20));
//
//        // --- TabPane for organized display ---
//        tabPane = new TabPane();
//        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE); // Prevent tabs from being closed
//
//        // Tab 1: Details (Original text display)
//        Tab detailsTab = new Tab("Full Details");
//        resultDisplay = new TextArea();
//        resultDisplay.setEditable(false);
//        resultDisplay.setWrapText(true);
//        resultDisplay.setPrefRowCount(25);
//        resultDisplay.setFont(Font.font("Monospaced", 13));
//        VBox detailsContent = new VBox(10);
//        detailsContent.setPadding(new Insets(10));
//        detailsContent.getChildren().add(resultDisplay);
//        detailsTab.setContent(detailsContent);
//
//        // Tab 2: Demographics Charts (Race & Age Pie Charts)
//        Tab demographicsChartsTab = new Tab("Demographics Charts");
//        VBox demographicsChartsContent = new VBox(20);
//        demographicsChartsContent.setAlignment(Pos.TOP_CENTER);
//        demographicsChartsContent.setPadding(new Insets(20));
//
//        racePieChart = new PieChart();
//        racePieChart.setTitle("Racial Distribution");
//        racePieChart.setLabelsVisible(true);
//        racePieChart.setLabelLineLength(10);
//        racePieChart.setLegendVisible(true);
//
//        agePieChart = new PieChart();
//        agePieChart.setTitle("Age Distribution");
//        agePieChart.setLabelsVisible(true);
//        agePieChart.setLabelLineLength(10);
//        agePieChart.setLegendVisible(true);
//
//        demographicsChartsContent.getChildren().addAll(racePieChart, agePieChart);
//        demographicsChartsTab.setContent(demographicsChartsContent);
//
//        // Tab 3: Schools
//        Tab schoolsTab = new Tab("Schools");
//        schoolsDisplayVBox = new VBox(15); // Increased spacing for schools
//        schoolsDisplayVBox.setPadding(new Insets(20));
//        schoolsDisplayVBox.setAlignment(Pos.TOP_LEFT);
//        schoolsTab.setContent(new ScrollPane(schoolsDisplayVBox)); // Use ScrollPane for many schools
//
//        // Tab 4: Real Estate & Income
//        Tab realEstateTab = new Tab("Real Estate & Income");
//        VBox realEstateContent = new VBox(15); // Spacing for real estate info
//        realEstateContent.setPadding(new Insets(20));
//        realEstateContent.setAlignment(Pos.TOP_LEFT);
//
//        Label realEstateTitle = new Label("Housing & Income Data");
//        realEstateTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
//
//        avgHomePriceLabel = new Label("Average Home Price: N/A");
//        avgHomePriceLabel.setFont(Font.font("Arial", 14));
//        avgRentLabel = new Label("Average Rent: N/A");
//        avgRentLabel.setFont(Font.font("Arial", 14));
//        medianIncomeLabel = new Label("Median Household Income: N/A");
//        medianIncomeLabel.setFont(Font.font("Arial", 14));
//
//        realEstateContent.getChildren().addAll(realEstateTitle, avgHomePriceLabel, avgRentLabel, medianIncomeLabel);
//        realEstateTab.setContent(realEstateContent);
//
//
//        // Tab 5: University Degrees
//        Tab degreesTab = new Tab("University Degrees");
//        VBox degreesContent = new VBox(20);
//        degreesContent.setAlignment(Pos.TOP_CENTER);
//        degreesContent.setPadding(new Insets(20));
//
//        xAxis = new CategoryAxis();
//        yAxis = new NumberAxis();
//        degreeBarChart = new BarChart<>(xAxis, yAxis);
//        degreeBarChart.setTitle("Highest Degree Attained");
//        xAxis.setLabel("Degree Type");
//        yAxis.setLabel("Population Estimate");
//        degreeBarChart.setLegendVisible(false); // Often cleaner for simple bar charts
//        degreeBarChart.setBarGap(5);
//        degreeBarChart.setCategoryGap(20);
//
//        // Set fixed preferred size for the bar chart
//        degreeBarChart.setPrefWidth(350);  // Adjust these values as needed
//        degreeBarChart.setPrefHeight(400); // Adjust these values as needed
//        // Prevent chart from growing/shrinking beyond preferred size
//        degreeBarChart.setMaxWidth(350);
//        degreeBarChart.setMaxHeight(400);
//
//        degreesContent.getChildren().add(degreeBarChart);
//        degreesTab.setContent(degreesContent);
//
//
//        // Add all tabs to the TabPane
//        tabPane.getTabs().addAll(detailsTab, demographicsChartsTab, schoolsTab, realEstateTab, degreesTab);
//
//
//        // --- Main Layout ---
//        BorderPane root = new BorderPane();
//        root.setTop(inputControls);
//        root.setCenter(tabPane);
//        BorderPane.setMargin(tabPane, new Insets(10, 20, 20, 20));
//
//        // Set up action for the button
//        fetchDataButton.setOnAction(event -> fetchDataForZip());
//
//
//        // --- Scene and Stage ---
//        Scene scene = new Scene(root, 1050, 900); // Adjusted size for more tabs and content
//
//        String css = ".axis-label {" +
//                "    -fx-font-size: 14px;" +   // Set font size
//                "    -fx-font-weight: bold;" + // Set font weight to bold
//                "    -fx-text-fill: #333;" +   // Optional: Set text color
//                "}" +
//                // CSS to style the bars in the BarChart
//                ".chart-bar {" +
//                "    -fx-bar-fill: #8A2BE2;" + // Deep purple color
//                "}";
//        scene.getStylesheets().add("data:text/css," + css);
//
//        primaryStage.setScene(scene);
//        primaryStage.show();
//    }
//
//    /**
//     * Handles the data fetching process when the button is clicked.
//     * Runs the backend logic on a separate thread and updates the UI on the JavaFX Application Thread.
//     */
//    private void fetchDataForZip() {
//        String zipText = zipCodeInput.getText().trim();
//        if (zipText.isEmpty()) {
//            showAlert("Input Error", "Please enter a ZIP code.");
//            return;
//        }
//
//        int zipCode;
//        try {
//            zipCode = Integer.parseInt(zipText);
//            if (zipText.length() != 5) {
//                throw new NumberFormatException("ZIP code must be 5 digits.");
//            }
//        } catch (NumberFormatException e) {
//            showAlert("Input Error", "Invalid ZIP code. Please enter a 5-digit number.");
//            return;
//        }
//
//        // Disable UI elements and show progress
//        fetchDataButton.setDisable(true);
//        zipCodeInput.setDisable(true);
//        progressBar.setVisible(true);
//        progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
//        statusLabel.setText("Fetching data for ZIP: " + zipCode + "...");
//        clearAllDisplays(); // Clear all display areas before new data load
//
//        final int finalZipCode = zipCode;
//
//        // Create a Task to run the backend operations on a background thread
//        Task<DetailedLocationInfo> fetchTask = new Task<DetailedLocationInfo>() {
//            @Override
//            protected DetailedLocationInfo call() throws Exception {
//                // Instantiate LocationQuery and fetch data
//                LocationQuery locationQuery = new LocationQuery(finalZipCode);
//                return locationQuery.fetchData();
//            }
//        };
//
//        // Set up event handlers for the task
//        fetchTask.setOnSucceeded(e -> {
//            DetailedLocationInfo info = fetchTask.getValue();
//            // Update all display components
//            displayDetails(info);
//            updateDemographicCharts(info);
//            displaySchools(info);
//            displayRealEstate(info);
//            updateDegreeChart(info);
//
//            resetUI();
//            statusLabel.setText("Data fetched successfully for the ZIP: " + finalZipCode);
//        });
//
//        fetchTask.setOnFailed(e -> {
//            Throwable error = fetchTask.getException();
//            System.err.println("Error during data fetch: " + error.getMessage());
//            error.printStackTrace(); // Print full stack trace for debugging
//            showAlert("Error", "Failed to fetch data: " + error.getMessage() + "\nPlease check your internet connection or the ZIP code.");
//            resultDisplay.setText("Error: " + error.getMessage() + "\nSee console for details.");
//            resetUI();
//            statusLabel.setText("Error fetching data.");
//        });
//
//        fetchTask.setOnCancelled(e -> {
//            showAlert("Operation Cancelled", "Data fetch was cancelled.");
//            resetUI();
//            statusLabel.setText("Operation cancelled.");
//        });
//
//        // Run the task on the executor service
//        executorService.submit(fetchTask);
//    }
//
//    /**
//     * Clears all display areas in preparation for new data.
//     */
//    private void clearAllDisplays() {
//        resultDisplay.clear();
//        racePieChart.setData(FXCollections.emptyObservableList());
//        agePieChart.setData(FXCollections.emptyObservableList());
//        schoolsDisplayVBox.getChildren().clear();
//        avgHomePriceLabel.setText("Average Home Price: N/A");
//        avgRentLabel.setText("Average Rent: N/A");
//        medianIncomeLabel.setText("Median Household Income: N/A");
//        degreeBarChart.setData(FXCollections.emptyObservableList()); // Clear bar chart
//    }
//
//    /**
//     * Resets the UI elements to their initial state after a fetch operation.
//     */
//    private void resetUI() {
//        fetchDataButton.setDisable(false);
//        zipCodeInput.setDisable(false);
//        progressBar.setVisible(false);
//        progressBar.setProgress(0);
//    }
//
//    /**
//     * Populates the 'Details' tab TextArea with comprehensive textual information.
//     * @param info The DetailedLocationInfo object.
//     */
//    private void displayDetails(DetailedLocationInfo info) {
//        StringBuilder sb = new StringBuilder();
//        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
//        NumberFormat percentFormat = NumberFormat.getPercentInstance(Locale.US);
//        percentFormat.setMinimumFractionDigits(1);
//        percentFormat.setMaximumFractionDigits(1);
//
//
//        // --- Location Information ---
//        sb.append("--- 📍 Location Information ---\n");
//        if (info.getCity() != null && !info.getCity().isEmpty()) {
//            sb.append(String.format("City, State: %s, %s (ZIP %d)\n", info.getCity(), info.getState(), info.getZipCode()));
//            sb.append(String.format("Coordinates: %.6f, %.6f\n", info.getLatitude(), info.getLongitude()));
//        } else {
//            sb.append("Location data not available.\n");
//        }
//
//
//        // --- Housing Information --- (Duplicated here for comprehensive details tab)
//        sb.append("\n--- 🏠 Housing Information ---\n");
//        if (info.getHousingInfo().avgHomePrice != -1) {
//            sb.append(String.format("Average Home Price: %s\n", currencyFormat.format(info.getHousingInfo().avgHomePrice)));
//        } else {
//            sb.append("Average home price not available.\n");
//        }
//        if (info.getHousingInfo().avgRent != -1) {
//            sb.append(String.format("Average Rent: %s\n", currencyFormat.format(info.getHousingInfo().avgRent)));
//        } else {
//            sb.append("Average rent not available.\n");
//        }
//
//
//        // --- Median Income --- (Duplicated here for comprehensive details tab)
//        sb.append("\n--- 💰 Median Income ---\n");
//        if (info.getMedianIncome() != -1) {
//            sb.append(String.format("Median Household Income: %s\n", currencyFormat.format(info.getMedianIncome())));
//        } else {
//            sb.append("Median household income not available.\n");
//        }
//
//
//        // --- Local NCES Schools --- (Duplicated here for comprehensive details tab)
//        sb.append("\n--- 🏫 Local NCES Schools (Top 5) ---\n");
//        List<LocalSchoolFinder.School> schools = info.getSchools();
//        if (schools != null && !schools.isEmpty()) {
//            for (int i = 0; i < schools.size(); i++) {
//                sb.append(String.format("%d. %s\n", i + 1, schools.get(i).toString()));
//            }
//        } else {
//            sb.append("No schools found for this ZIP code.\n");
//        }
//
//
//        // --- Demographic Snapshot (Textual) --- (Duplicated here for comprehensive details tab)
//        sb.append("\n--- 📊 Demographic Snapshot ---\n");
//        Map<String, String> demographics = info.getDemographics();
//        if (demographics != null && !demographics.isEmpty()) {
//            int total = Integer.parseInt(demographics.getOrDefault("B01003_001E", "0"));
//            sb.append(String.format("Total Population: %,d\n", total));
//
//            if (total > 0) {
//                sb.append(String.format("White: %,d (%s)\n",
//                        Integer.parseInt(demographics.getOrDefault("B02001_002E", "0")),
//                        percentFormat.format(Integer.parseInt(demographics.getOrDefault("B02001_002E", "0")) * 1.0 / total)));
//                sb.append(String.format("Black: %,d (%s)\n",
//                        Integer.parseInt(demographics.getOrDefault("B02001_003E", "0")),
//                        percentFormat.format(Integer.parseInt(demographics.getOrDefault("B02001_003E", "0")) * 1.0 / total)));
//                sb.append(String.format("American Indian/Alaska Native: %,d (%s)\n",
//                        Integer.parseInt(demographics.getOrDefault("B02001_004E", "0")),
//                        percentFormat.format(Integer.parseInt(demographics.getOrDefault("B02001_004E", "0")) * 1.0 / total)));
//                sb.append(String.format("Asian: %,d (%s)\n",
//                        Integer.parseInt(demographics.getOrDefault("B02001_005E", "0")),
//                        percentFormat.format(Integer.parseInt(demographics.getOrDefault("B02001_005E", "0")) * 1.0 / total)));
//                sb.append(String.format("Native Hawaiian/Pacific Islander: %,d (%s)\n",
//                        Integer.parseInt(demographics.getOrDefault("B02001_006E", "0")),
//                        percentFormat.format(Integer.parseInt(demographics.getOrDefault("B02001_006E", "0")) * 1.0 / total)));
//                sb.append(String.format("Two or More Races: %,d (%s)\n",
//                        Integer.parseInt(demographics.getOrDefault("B02001_008E", "0")),
//                        percentFormat.format(Integer.parseInt(demographics.getOrDefault("B02001_008E", "0")) * 1.0 / total)));
//                sb.append(String.format("Other/Latino: %,d (%s)\n",
//                        Integer.parseInt(demographics.getOrDefault("B02001_007E", "0")),
//                        percentFormat.format(Integer.parseInt(demographics.getOrDefault("B02001_007E", "0")) * 1.0 / total)));
//
//                sb.append("\n");
//                sb.append("\nDegrees Breakdown:\n");
//                sb.append(String.format("Bachelor's Degrees: %s\n", demographics.getOrDefault("B15003_017E", "N/A")));
//                sb.append(String.format("Doctorate Degrees: %s\n", demographics.getOrDefault("B15003_022E", "N/A")));
//
//
//                // Age Distribution
//                sb.append("\nAge Distribution:\n");
//                int under18 = info.getPopulationForAgeGroup("under18");
//                int age18to44 = info.getPopulationForAgeGroup("age18to44");
//                int age45to64 = info.getPopulationForAgeGroup("age45to64");
//                int age65plus = info.getPopulationForAgeGroup("age65plus");
//
//                if (total > 0) { // Ensure total population is not zero to avoid division by zero
//                    sb.append(String.format("Under 18: %,d (%s)\n", under18, percentFormat.format(under18 * 1.0 / total)));
//                    sb.append(String.format("18 to 44: %,d (%s)\n", age18to44, percentFormat.format(age18to44 * 1.0 / total)));
//                    sb.append(String.format("45 to 64: %,d (%s)\n", age45to64, percentFormat.format(age45to64 * 1.0 / total)));
//                    sb.append(String.format("65 and over: %,d (%s)\n", age65plus, percentFormat.format(age65plus * 1.0 / total)));
//                } else {
//                    sb.append("Age distribution not available due to zero total population.\n");
//                }
//            } else {
//                sb.append("Demographic data not available or total population is zero.\n");
//            }
//        } else {
//            sb.append("Demographic data not available.\n");
//        }
//
//        resultDisplay.setText(sb.toString());
//    }
//
//    /**
//     * Updates the PieCharts with demographic data.
//     * @param info The DetailedLocationInfo object containing all data.
//     */
//    private void updateDemographicCharts(DetailedLocationInfo info) {
//        Map<String, String> demographics = info.getDemographics();
//        if (demographics == null || demographics.isEmpty()) {
//            racePieChart.setData(FXCollections.emptyObservableList());
//            agePieChart.setData(FXCollections.emptyObservableList());
//            racePieChart.setTitle("Racial Distribution (Data Not Available)");
//            agePieChart.setTitle("Age Distribution (Data Not Available)");
//            return;
//        }
//
//        int totalPopulation = Integer.parseInt(demographics.getOrDefault("B01003_001E", "0"));
//
//        // --- Race Pie Chart Data ---
//        ObservableList<PieChart.Data> racePieChartData = FXCollections.observableArrayList();
//        if (totalPopulation > 0) {
//            addPieData(racePieChartData, "White", Integer.parseInt(demographics.getOrDefault("B02001_002E", "0")), totalPopulation);
//            addPieData(racePieChartData, "Black", Integer.parseInt(demographics.getOrDefault("B02001_003E", "0")), totalPopulation);
//            addPieData(racePieChartData, "Am. Indian/Alaskan Native", Integer.parseInt(demographics.getOrDefault("B02001_004E", "0")), totalPopulation);
//            addPieData(racePieChartData, "Asian", Integer.parseInt(demographics.getOrDefault("B02001_005E", "0")), totalPopulation);
//            addPieData(racePieChartData, "Nat. Hawaiian/Pacific Islander", Integer.parseInt(demographics.getOrDefault("B02001_006E", "0")), totalPopulation);
//            addPieData(racePieChartData, "Other/Latino", Integer.parseInt(demographics.getOrDefault("B02001_007E", "0")), totalPopulation); // Preserving user's label
//            addPieData(racePieChartData, "Two or More Races", Integer.parseInt(demographics.getOrDefault("B02001_008E", "0")), totalPopulation);
//        } else {
//            racePieChartData.add(new PieChart.Data("No Data", 1));
//        }
//        racePieChart.setData(racePieChartData);
//        racePieChart.setTitle("Racial Distribution");
//
//
//        // --- Age Pie Chart Data ---
//        ObservableList<PieChart.Data> agePieChartData = FXCollections.observableArrayList();
//        if (totalPopulation > 0) {
//            int under18 = info.getPopulationForAgeGroup("under18");
//            int age18to44 = info.getPopulationForAgeGroup("age18to44");
//            int age45to64 = info.getPopulationForAgeGroup("age45to64");
//            int age65plus = info.getPopulationForAgeGroup("age65plus");
//
//            addPieData(agePieChartData, "Under 18", under18, totalPopulation);
//            addPieData(agePieChartData, "18 to 44", age18to44, totalPopulation);
//            addPieData(agePieChartData, "45 to 64", age45to64, totalPopulation);
//            addPieData(agePieChartData, "65 and over", age65plus, totalPopulation);
//        } else {
//            agePieChartData.add(new PieChart.Data("No Data", 1));
//        }
//        agePieChart.setData(agePieChartData);
//        agePieChart.setTitle("Age Distribution");
//    }
//
//    /**
//     * Displays the school information in the 'Schools' tab.
//     * @param info The DetailedLocationInfo object.
//     */
//    private void displaySchools(DetailedLocationInfo info) {
//        schoolsDisplayVBox.getChildren().clear(); // Clear previous school data
//
//        List<LocalSchoolFinder.School> schools = info.getSchools();
//        if (schools != null && !schools.isEmpty()) {
//            Label header = new Label("Local NCES Schools (Top 5)");
//            header.setFont(Font.font("Arial", FontWeight.BOLD, 16));
//            schoolsDisplayVBox.getChildren().add(header);
//
//            for (LocalSchoolFinder.School school : schools) {
//                VBox schoolCard = new VBox(5);
//                schoolCard.setPadding(new Insets(10));
//                schoolCard.setStyle("-fx-border-color: #ccc; -fx-border-radius: 5; -fx-background-color: #f9f9f9;");
//
//                Label nameLabel = new Label(school.name);
//                nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
//                nameLabel.setWrapText(true);
//
//                Label levelLabel = new Label("Level: " + school.level);
//                Label locationLabel = new Label(school.city + ", " + school.state + " " + school.zip);
//
//                schoolCard.getChildren().addAll(nameLabel, levelLabel, locationLabel);
//                schoolsDisplayVBox.getChildren().add(schoolCard);
//            }
//        } else {
//            Label noSchoolsLabel = new Label("No schools found for this ZIP code.");
//            noSchoolsLabel.setFont(Font.font("Arial", FontWeight.THIN, 14));// WAS ITALIC
//            schoolsDisplayVBox.getChildren().add(noSchoolsLabel);
//        }
//    }
//
//    /**
//     * Displays the real estate and income information in the 'Real Estate & Income' tab.
//     * @param info The DetailedLocationInfo object.
//     */
//    private void displayRealEstate(DetailedLocationInfo info) {
//        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
//
//        if (info.getHousingInfo().avgHomePrice != -1) {
//            avgHomePriceLabel.setText(String.format("Average Home Price: %s", currencyFormat.format(info.getHousingInfo().avgHomePrice)));
//        } else {
//            avgHomePriceLabel.setText("Average Home Price: N/A");
//        }
//
//        if (info.getHousingInfo().avgRent != -1) {
//            avgRentLabel.setText(String.format("Average Rent: %s", currencyFormat.format(info.getHousingInfo().avgRent)));
//        } else {
//            avgRentLabel.setText("Average Rent: N/A");
//        }
//
//        if (info.getMedianIncome() != -1) {
//            medianIncomeLabel.setText(String.format("Median Household Income: %s", currencyFormat.format(info.getMedianIncome())));
//        } else {
//            medianIncomeLabel.setText("Median Household Income: N/A");
//        }
//    }
//
//    /**
//     * Updates the BarChart for university degrees.
//     * @param info The DetailedLocationInfo object.
//     */
//    private void updateDegreeChart(DetailedLocationInfo info) {
//        Map<String, String> demographics = info.getDemographics();
//        // Clear previous data by setting an empty, modifiable list
//        degreeBarChart.setData(FXCollections.observableArrayList());
//
//        if (demographics != null && !demographics.isEmpty()) {
//            XYChart.Series<String, Number> series = new XYChart.Series<>();
//            series.setName("Degrees"); // Optional: Name for the series
//
//            // Safely parse degree counts, defaulting to 0 if not found or invalid
//            int bachelors = parseIntOrDefault(demographics, "B15003_017E");
//            int doctorates = parseIntOrDefault(demographics, "B15003_022E");
//
//            if (bachelors > 0) {
//                series.getData().add(new XYChart.Data<>("Bachelor's Degrees", bachelors));
//            }
//            if (doctorates > 0) {
//                series.getData().add(new XYChart.Data<>("Doctorate Degrees", doctorates));
//            }
//
//            if (!series.getData().isEmpty()) {
//                degreeBarChart.getData().add(series);
//                degreeBarChart.setTitle("Highest Degree Attained");
//            } else {
//                degreeBarChart.setTitle("Highest Degree Attained (Data Not Available)");
//            }
//        } else {
//            degreeBarChart.setTitle("Highest Degree Attained (Data Not Available)");
//        }
//    }
//
//    /**
//     * Helper method to safely parse an integer from the demographics map.
//     * Returns 0 if the key is not found or the value is not a valid integer.
//     */
//    private int parseIntOrDefault(Map<String, String> data, String key) {
//        try {
//            String value = data.getOrDefault(key, "0");
//            if ("null".equalsIgnoreCase(value) || "-".equals(value) || value.isEmpty()) {
//                return 0;
//            }
//            return Integer.parseInt(value);
//        } catch (NumberFormatException e) {
//            System.err.println("Warning: Could not parse integer for key: " + key + ". Value was: " + data.get(key) + ". Error: " + e.getMessage());
//            return 0;
//        }
//    }
//
//
//    /**
//     * Helper method to add data to a PieChart.Data list.
//     * Only adds if the value is greater than 0 to avoid cluttering the chart with zero-percentage slices.
//     */
//    private void addPieData(ObservableList<PieChart.Data> pieDataList, String name, int count, int total) {
//        if (total > 0 && count > 0) {
//            double percentage = (double) count / total;
//            // Format for label: "Label (XX.X%)"
//            String label = String.format("%s (%.1f%%)", name, percentage * 100);
//            pieDataList.add(new PieChart.Data(label, count));
//        }
//    }
//
//
//    /**
//     * Shows a standard JavaFX alert dialog.
//     * @param title The title of the alert.
//     * @param message The message content of the alert.
//     */
//    private void showAlert(String title, String message) {
//        Platform.runLater(() -> {
//            Alert alert = new Alert(Alert.AlertType.ERROR);
//            alert.setTitle(title);
//            alert.setHeaderText(null);
//            alert.setContentText(message);
//            alert.showAndWait();
//        });
//    }
//
//    @Override
//    public void stop() {
//        // Shutdown the executor service when the application closes
//        if (executorService != null) {
//            executorService.shutdownNow();
//        }
//    }
//
//    public static void main(String[] args) {
//        launch(args);
//    }
//}







//// LocationApp.java
//package org.example;
//
//import javafx.application.Application;
//import javafx.application.Platform;
//import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;
//import javafx.concurrent.Task;
//import javafx.geometry.Insets;
//import javafx.geometry.Pos;
//import javafx.scene.Scene;
//import javafx.scene.chart.PieChart;
//import javafx.scene.control.*;
//import javafx.scene.layout.BorderPane;
//import javafx.scene.layout.GridPane;
//import javafx.scene.layout.VBox;
//import javafx.scene.text.Font;
//import javafx.scene.text.FontWeight;
//import javafx.scene.text.Text;
//import javafx.stage.Stage;
//
//import java.text.NumberFormat;
//import java.util.List;
//import java.util.Locale;
//import java.util.Map;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//public class LocationApp extends Application {
//
//    private TextField zipCodeInput;
//    private Button fetchDataButton;
//    private TextArea resultDisplay;
//    private ProgressBar progressBar;
//    private Label statusLabel;
//    private TabPane tabPane; // New TabPane
//    private PieChart racePieChart; // New PieChart for Race
//    private PieChart agePieChart;  // New PieChart for Age
//
//    // ExecutorService for background tasks to keep UI responsive
//    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
//
//    @Override
//    public void start(Stage primaryStage) {
//        primaryStage.setTitle("Location Information App");
//
//        // --- UI Elements ---
//        zipCodeInput = new TextField();
//        zipCodeInput.setPromptText("Enter 5-digit ZIP code (e.g., 30336)");
//        zipCodeInput.setMaxWidth(200);
//        zipCodeInput.setAlignment(Pos.CENTER);
//        zipCodeInput.setText("30336"); // Default ZIP for convenience
//
//        fetchDataButton = new Button("Lookup");
//        fetchDataButton.setFont(Font.font("Arial", FontWeight.BOLD, 14));
//        fetchDataButton.setPadding(new Insets(10, 20, 10, 20));
//
//        progressBar = new ProgressBar(0);
//        progressBar.setVisible(false); // Hidden initially
//        progressBar.setPrefWidth(200);
//
//        statusLabel = new Label("Ready");
//        statusLabel.setFont(Font.font("Arial", 12));
//        statusLabel.setStyle("-fx-text-fill: #333;");
//
//        resultDisplay = new TextArea();
//        resultDisplay.setEditable(false);
//        resultDisplay.setWrapText(true);
//        resultDisplay.setPrefRowCount(25); // Adjust based on expected content length
//        resultDisplay.setFont(Font.font("Monospaced", 13)); // Consistent font for data display
//
//        // --- TabPane for organized display ---
//        tabPane = new TabPane();
//        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE); // Prevent tabs from being closed
//
//        // Textual Data Tab
//        Tab textTab = new Tab("Details");
//        VBox textContent = new VBox(10);
//        textContent.setPadding(new Insets(10));
//        textContent.getChildren().add(resultDisplay);
//        textTab.setContent(textContent);
//
//        // Demographic Charts Tab
//        Tab chartsTab = new Tab("Demographics Charts");
//        VBox chartsContent = new VBox(20); // Spacing between charts
//        chartsContent.setAlignment(Pos.TOP_CENTER);
//        chartsContent.setPadding(new Insets(20));
//
//        // Race Pie Chart
//        racePieChart = new PieChart();
//        racePieChart.setTitle("Racial Distribution");
//        racePieChart.setLabelsVisible(true);
//        racePieChart.setLabelLineLength(10);
//        racePieChart.setLegendVisible(true);
//
//        // Age Pie Chart
//        agePieChart = new PieChart();
//        agePieChart.setTitle("Age Distribution");
//        agePieChart.setLabelsVisible(true);
//        agePieChart.setLabelLineLength(10);
//        agePieChart.setLegendVisible(true);
//
//        chartsContent.getChildren().addAll(racePieChart, agePieChart);
//        chartsTab.setContent(chartsContent);
//
//        tabPane.getTabs().addAll(textTab, chartsTab);
//
//
//        // --- Layout ---
//        VBox inputControls = new VBox(10, zipCodeInput, fetchDataButton, progressBar, statusLabel);
//        inputControls.setAlignment(Pos.CENTER);
//        inputControls.setPadding(new Insets(20));
//
//        BorderPane root = new BorderPane();
//        root.setTop(inputControls);
//        root.setCenter(tabPane); // Set the TabPane as the center content
//        BorderPane.setMargin(tabPane, new Insets(10, 20, 20, 20)); // Add margins around tab pane
//
//        // Set up action for the button
//        fetchDataButton.setOnAction(event -> fetchDataForZip());
//
//        // --- Scene and Stage ---
//        Scene scene = new Scene(root, 700, 850); // Increased height to accommodate charts
//        primaryStage.setScene(scene);
//        primaryStage.show();
//    }
//
//    /**
//     * Handles the data fetching process when the button is clicked.
//     * Runs the backend logic on a separate thread and updates the UI on the JavaFX Application Thread.
//     */
//    private void fetchDataForZip() {
//        String zipText = zipCodeInput.getText().trim();
//        if (zipText.isEmpty()) {
//            showAlert("Input Error", "Please enter a ZIP code.");
//            return;
//        }
//
//        int zipCode;
//        try {
//            zipCode = Integer.parseInt(zipText);
//            if (zipText.length() != 5) {
//                throw new NumberFormatException("ZIP code must be 5 digits.");
//            }
//        } catch (NumberFormatException e) {
//            showAlert("Input Error", "Invalid ZIP code. Please enter a 5-digit number.");
//            return;
//        }
//
//        // Disable UI elements and show progress
//        fetchDataButton.setDisable(true);
//        zipCodeInput.setDisable(true);
//        progressBar.setVisible(true);
//        progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
//        statusLabel.setText("Fetching data for ZIP: " + zipCode + "...");
//        resultDisplay.clear(); // Clear text display
//        racePieChart.setData(FXCollections.emptyObservableList()); // Clear charts
//        agePieChart.setData(FXCollections.emptyObservableList());
//
//        final int finalZipCode = zipCode;
//
//        // Create a Task to run the backend operations on a background thread
//        Task<DetailedLocationInfo> fetchTask = new Task<DetailedLocationInfo>() {
//            @Override
//            protected DetailedLocationInfo call() throws Exception {
//                // Instantiate LocationQuery and fetch data
//                LocationQuery locationQuery = new LocationQuery(finalZipCode);
//                return locationQuery.fetchData();
//            }
//        };
//
//        // Set up event handlers for the task
//        fetchTask.setOnSucceeded(e -> {
//            DetailedLocationInfo info = fetchTask.getValue();
//            displayResults(info); // Update text display
//            updateCharts(info);   // Update charts
//            resetUI();
//            statusLabel.setText("Data fetched successfully for the ZIP: " + finalZipCode);
//        });
//
//        fetchTask.setOnFailed(e -> {
//            Throwable error = fetchTask.getException();
//            System.err.println("Error during data fetch: " + error.getMessage());
//            error.printStackTrace(); // Print full stack trace for debugging
//            showAlert("Error", "Failed to fetch data: " + error.getMessage() + "\nPlease check your internet connection or the ZIP code.");
//            resultDisplay.setText("Error: " + error.getMessage() + "\nSee console for details.");
//            resetUI();
//            statusLabel.setText("Error fetching data.");
//        });
//
//        fetchTask.setOnCancelled(e -> {
//            showAlert("Operation Cancelled", "Data fetch was cancelled.");
//            resetUI();
//            statusLabel.setText("Operation cancelled.");
//        });
//
//        // Run the task on the executor service
//        executorService.submit(fetchTask);
//    }
//
//    /**
//     * Resets the UI elements to their initial state after a fetch operation.
//     */
//    private void resetUI() {
//        fetchDataButton.setDisable(false);
//        zipCodeInput.setDisable(false);
//        progressBar.setVisible(false);
//        progressBar.setProgress(0);
//    }
//
//    /**
//     * Displays the fetched DetailedLocationInfo in the resultDisplay TextArea.
//     *
//     * @param info The DetailedLocationInfo object containing all data.
//     */
//    private void displayResults(DetailedLocationInfo info) {
//        StringBuilder sb = new StringBuilder();
//        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
//        NumberFormat percentFormat = NumberFormat.getPercentInstance(Locale.US);
//        percentFormat.setMinimumFractionDigits(1);
//        percentFormat.setMaximumFractionDigits(1);
//
//
//        // --- Location Information ---
//        sb.append("--- 📍 Location Information ---\n");
//        if (info.getCity() != null && !info.getCity().isEmpty()) {
//            sb.append(String.format("City, State: %s, %s (ZIP %d)\n", info.getCity(), info.getState(), info.getZipCode()));
//            sb.append(String.format("Coordinates: %.6f, %.6f\n", info.getLatitude(), info.getLongitude()));
//        } else {
//            sb.append("Location data not available.\n");
//        }
//
//
//        // --- Housing Information ---
//        sb.append("\n--- 🏠 Housing Information ---\n");
//        if (info.getHousingInfo().avgHomePrice != -1) {
//            sb.append(String.format("Average Home Price: %s\n", currencyFormat.format(info.getHousingInfo().avgHomePrice)));
//        } else {
//            sb.append("Average home price not available.\n");
//        }
//        if (info.getHousingInfo().avgRent != -1) {
//            sb.append(String.format("Average Rent: %s\n", currencyFormat.format(info.getHousingInfo().avgRent)));
//        } else {
//            sb.append("Average rent not available.\n");
//        }
//
//
//        // --- Median Income ---
//        sb.append("\n--- 💰 Median Income ---\n");
//        if (info.getMedianIncome() != -1) {
//            sb.append(String.format("Median Household Income: %s\n", currencyFormat.format(info.getMedianIncome())));
//        } else {
//            sb.append("Median household income not available.\n");
//        }
//
//
//        // --- Local NCES Schools ---
//        sb.append("\n--- 🏫 Local NCES Schools (Top 5) ---\n");
//        List<LocalSchoolFinder.School> schools = info.getSchools();
//        if (schools != null && !schools.isEmpty()) {
//            for (int i = 0; i < schools.size(); i++) {
//                sb.append(String.format("%d. %s\n", i + 1, schools.get(i).toString()));
//            }
//        } else {
//            sb.append("No schools found for this ZIP code.\n");
//        }
//
//
//        // --- Demographic Snapshot (Textual) ---
//        sb.append("\n--- 📊 Demographic Snapshot ---\n");
//        Map<String, String> demographics = info.getDemographics();
//        if (demographics != null && !demographics.isEmpty()) {
//            int total = Integer.parseInt(demographics.getOrDefault("B01003_001E", "0"));
//            sb.append(String.format("Total Population: %,d\n", total));
//
//            if (total > 0) {
//                sb.append(String.format("White: %,d (%s)\n",
//                        Integer.parseInt(demographics.getOrDefault("B02001_002E", "0")),
//                        percentFormat.format(Integer.parseInt(demographics.getOrDefault("B02001_002E", "0")) * 1.0 / total)));
//                sb.append(String.format("Black: %,d (%s)\n",
//                        Integer.parseInt(demographics.getOrDefault("B02001_003E", "0")),
//                        percentFormat.format(Integer.parseInt(demographics.getOrDefault("B02001_003E", "0")) * 1.0 / total)));
//                sb.append(String.format("American Indian/Alaska Native: %,d (%s)\n",
//                        Integer.parseInt(demographics.getOrDefault("B02001_004E", "0")),
//                        percentFormat.format(Integer.parseInt(demographics.getOrDefault("B02001_004E", "0")) * 1.0 / total)));
//                sb.append(String.format("Asian: %,d (%s)\n",
//                        Integer.parseInt(demographics.getOrDefault("B02001_005E", "0")),
//                        percentFormat.format(Integer.parseInt(demographics.getOrDefault("B02001_005E", "0")) * 1.0 / total)));
//                sb.append(String.format("Native Hawaiian/Pacific Islander: %,d (%s)\n",
//                        Integer.parseInt(demographics.getOrDefault("B02001_006E", "0")),
//                        percentFormat.format(Integer.parseInt(demographics.getOrDefault("B02001_006E", "0")) * 1.0 / total)));
//                sb.append(String.format("Two or More Races: %,d (%s)\n",
//                        Integer.parseInt(demographics.getOrDefault("B02001_008E", "0")),
//                        percentFormat.format(Integer.parseInt(demographics.getOrDefault("B02001_008E", "0")) * 1.0 / total)));
//                sb.append(String.format("Other/Latino: %,d (%s)\n",
//                        Integer.parseInt(demographics.getOrDefault("B02001_007E", "0")),
//                        percentFormat.format(Integer.parseInt(demographics.getOrDefault("B02001_007E", "0")) * 1.0 / total)));
//
//                sb.append("\n");
//                sb.append("\nDegrees Breakdown:\n");
//                sb.append(String.format("Bachelor's Degrees: %s\n", demographics.getOrDefault("B15003_017E", "N/A")));
//                sb.append(String.format("Doctorate Degrees: %s\n", demographics.getOrDefault("B15003_022E", "N/A")));
//
//
//                // Age Distribution
//                sb.append("\nAge Distribution:\n");
//                int under18 = info.getPopulationForAgeGroup("under18");
//                int age18to44 = info.getPopulationForAgeGroup("age18to44");
//                int age45to64 = info.getPopulationForAgeGroup("age45to64");
//                int age65plus = info.getPopulationForAgeGroup("age65plus");
//
//                if (total > 0) { // Ensure total population is not zero to avoid division by zero
//                    sb.append(String.format("Under 18: %,d (%s)\n", under18, percentFormat.format(under18 * 1.0 / total)));
//                    sb.append(String.format("18 to 44: %,d (%s)\n", age18to44, percentFormat.format(age18to44 * 1.0 / total)));
//                    sb.append(String.format("45 to 64: %,d (%s)\n", age45to64, percentFormat.format(age45to64 * 1.0 / total)));
//                    sb.append(String.format("65 and over: %,d (%s)\n", age65plus, percentFormat.format(age65plus * 1.0 / total)));
//                } else {
//                    sb.append("Age distribution not available due to zero total population.\n");
//                }
//            } else {
//                sb.append("Demographic data not available or total population is zero.\n");
//            }
//        } else {
//            sb.append("Demographic data not available.\n");
//        }
//
//        resultDisplay.setText(sb.toString());
//    }
//
//    /**
//     * Updates the PieCharts with demographic data.
//     * @param info The DetailedLocationInfo object containing all data.
//     */
//    private void updateCharts(DetailedLocationInfo info) {
//        Map<String, String> demographics = info.getDemographics();
//        if (demographics == null || demographics.isEmpty()) {
//            racePieChart.setData(FXCollections.emptyObservableList());
//            agePieChart.setData(FXCollections.emptyObservableList());
//            racePieChart.setTitle("Racial Distribution (Data Not Available)");
//            agePieChart.setTitle("Age Distribution (Data Not Available)");
//            return;
//        }
//
//        int totalPopulation = Integer.parseInt(demographics.getOrDefault("B01003_001E", "0"));
//
//        // --- Race Pie Chart Data ---
//        ObservableList<PieChart.Data> racePieChartData = FXCollections.observableArrayList();
//        if (totalPopulation > 0) {
//            addPieData(racePieChartData, "White", Integer.parseInt(demographics.getOrDefault("B02001_002E", "0")), totalPopulation);
//            addPieData(racePieChartData, "Black", Integer.parseInt(demographics.getOrDefault("B02001_003E", "0")), totalPopulation);
//            addPieData(racePieChartData, "Am. Indian/Alaskan Native", Integer.parseInt(demographics.getOrDefault("B02001_004E", "0")), totalPopulation);
//            addPieData(racePieChartData, "Asian", Integer.parseInt(demographics.getOrDefault("B02001_005E", "0")), totalPopulation);
//            addPieData(racePieChartData, "Nat. Hawaiian/Pacific Islander", Integer.parseInt(demographics.getOrDefault("B02001_006E", "0")), totalPopulation);
//            addPieData(racePieChartData, "Other/Latino", Integer.parseInt(demographics.getOrDefault("B02001_007E", "0")), totalPopulation);
//            addPieData(racePieChartData, "Two or More Races", Integer.parseInt(demographics.getOrDefault("B02001_008E", "0")), totalPopulation);
//        } else {
//            racePieChartData.add(new PieChart.Data("No Data", 1)); // Placeholder for no data
//        }
//        racePieChart.setData(racePieChartData);
//        racePieChart.setTitle("Racial Distribution");
//
//
//        // --- Age Pie Chart Data ---
//        ObservableList<PieChart.Data> agePieChartData = FXCollections.observableArrayList();
//        if (totalPopulation > 0) {
//            int under18 = info.getPopulationForAgeGroup("under18");
//            int age18to44 = info.getPopulationForAgeGroup("age18to44");
//            int age45to64 = info.getPopulationForAgeGroup("age45to64");
//            int age65plus = info.getPopulationForAgeGroup("age65plus");
//
//            addPieData(agePieChartData, "Under 18", under18, totalPopulation);
//            addPieData(agePieChartData, "18 to 44", age18to44, totalPopulation);
//            addPieData(agePieChartData, "45 to 64", age45to64, totalPopulation);
//            addPieData(agePieChartData, "65 and over", age65plus, totalPopulation);
//        } else {
//            agePieChartData.add(new PieChart.Data("No Data", 1)); // Placeholder for no data
//        }
//        agePieChart.setData(agePieChartData);
//        agePieChart.setTitle("Age Distribution");
//    }
//
//    /**
//     * Helper method to add data to a PieChart.Data list.
//     * Only adds if the value is greater than 0 to avoid cluttering the chart with zero-percentage slices.
//     */
//    private void addPieData(ObservableList<PieChart.Data> pieDataList, String name, int count, int total) {
//        if (total > 0 && count > 0) {
//            double percentage = (double) count / total;
//            // Format for label: "Label (XX.X%)"
//            String label = String.format("%s (%.1f%%)", name, percentage * 100);
//            pieDataList.add(new PieChart.Data(label, count));
//        }
//    }
//
//
//    /**
//     * Shows a standard JavaFX alert dialog.
//     * @param title The title of the alert.
//     * @param message The message content of the alert.
//     */
//    private void showAlert(String title, String message) {
//        Platform.runLater(() -> {
//            Alert alert = new Alert(Alert.AlertType.ERROR);
//            alert.setTitle(title);
//            alert.setHeaderText(null);
//            alert.setContentText(message);
//            alert.showAndWait();
//        });
//    }
//
//    @Override
//    public void stop() {
//        // Shutdown the executor service when the application closes
//        if (executorService != null) {
//            executorService.shutdownNow();
//        }
//    }
//
//    public static void main(String[] args) {
//        launch(args);
//    }
//}
//
//
//
//
//
//
//

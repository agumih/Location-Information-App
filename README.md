# Location Information App (JavaFX Desktop)

## Overview
This is a JavaFX desktop application designed to provide comprehensive, interactive information about a given U.S. ZIP code. It fetches and displays various data points, including demographics, real estate trends, median income, and details about local schools.

## Features
* **Demographics:** Visualizes population distribution by race and age using interactive pie charts.

* **Real Estate & Income:** Displays average home prices, rents, and median household income.

* **Local Schools:** Lists top local schools with detailed information available via clickable pop-ups (including school type, charter status, address, phone, and website).

## How to Run the Application
This project is a JavaFX application built with Maven. It can be easily run from popular IDEs like IntelliJ IDEA or directly from your command line.

### Prerequisites
**1. Java Development Kit (JDK) 21 or higher.**

- You can check your current JDK version by running java -version in your terminal or command prompt.

- If you need to install it, consider using SDKMAN! (sdk install java 21.0.1-tem for example), or download an installer from Adoptium (Eclipse Temurin) or Oracle.

**2. JavaFX SDK 21.0.1 (or compatible with your JDK 21).**

- Download the JavaFX SDK for JDK 21 for your specific operating system (macOS, Windows, or Linux) from the official OpenJFX download page: https://gluonhq.com/products/javafx/.

- Unzip the downloaded file to a convenient, memorable location on your computer (e.g., /Users/yourusername/javafx-sdk-21.0.1 on macOS/Linux, or C:\javafx-sdk-21.0.1 on Windows).

**3. Set the PATH_TO_FX Environment Variable:**
This is **CRUCIAL** for Maven and your IDE to find the JavaFX modules at runtime. The variable needs to point to the lib directory inside your unzipped JavaFX SDK.

- On macOS/Linux (add to ~/.zshrc or ~/.bashrc for persistence):

      export PATH_TO_FX="/path/to/your/javafx-sdk-21.0.1/lib"

    (Replace /path/to/your/javafx-sdk-21.0.1/lib with the actual path where you unzipped JavaFX. After adding, open a new terminal window or run source ~/.zshrc / source ~/.bashrc to       apply changes).

- On Windows (Command Prompt - temporary, or set permanently via System Environment Variables):

      set PATH_TO_FX=C:\path\to\your\javafx-sdk-21.0.1\lib

    (For permanent setting: Search "Environment Variables" in Windows Start Menu, click "Edit the system environment variables", then "Environment Variables..." button, then add a           "New..." System variable named PATH_TO_FX with the path to your JavaFX lib folder. You may need to restart your command prompt or computer for permanent changes to take effect).

### Steps to Run:
**1. Clone the Repository:**
        Open your terminal or command prompt and clone this GitHub repository:
        
        git clone [https://github.com/agumih/Location-Information-App.git]
        

**2. Ensure pom.xml is up-to-date:**
Verify that your pom.xml file matches the one provided in the repository, especially the <artifactId>LocationApp</artifactId> and the javafx-maven-plugin configuration for mainClass and vmArgs.

**3. Open in IntelliJ IDEA (Recommended):**

- Launch IntelliJ IDEA.

- Select "Open" and navigate to the cloned project directory (the folder containing pom.xml).

- IntelliJ should recognize it as a Maven project and import it automatically. Confirm that it's using JDK 21 for the project SDK.

- In IntelliJ's Maven sidebar (View > Tool Windows > Maven), expand LocationApp > Plugins > javafx.

- Double-click the javafx:run goal to launch the application.

**4. Run from Command Line:**
After cloning the repository and ensuring the PATH_TO_FX environment variable is set (as described in Prerequisites), navigate to the project's root directory (where pom.xml is located) in your terminal or command prompt:

- On Linux/macOS:

      ./mvnw javafx:run

- On Windows (in Command Prompt):

      .\mvnw.cmd javafx:run

The JavaFX application window should then launch. You can enter a 5-digit U.S. ZIP code (e.g., 30336, 90210, 10001, 78704) into the input field and click the "Lookup" button to fetch and display the location data.

## API Key Information (Important Note)
For the convenience of demonstration and immediate testing by any user, the necessary API keys for external data fetching (Zipcodebase and Rentcast) are currently embedded directly within the source code.

In a real-world, production application, these API keys would **never** be committed to a public repository. This approach was chosen solely to facilitate a seamless and immediate testing experience for this project, and also, the API keys have limited calls.

# ELD Fixer 2.0

## Overview

ELD Fixer 2.0 automates the troubleshooting process for ELD devices reported in Slack.

When a device ID is posted in the configured Slack channel, the application automatically:

* Monitors Slack messages in real time using Socket Mode.
* Checks whether the device already exists in the Google Sheets tracker.
* Increments the repair counter for existing devices.
* Adds the appropriate Slack reaction based on the result.
* Retrieves the latest BLE packet information from Skyonics.
* Detects whether the device is powered off or actively reporting.
* Automatically sends recovery commands when required.
* Verifies whether the commands were successful.
* Adds new devices to the Google Sheet when necessary.
* Updates the spreadsheet automatically without any manual intervention.

The goal of version 2.0 is to eliminate the majority of manual troubleshooting steps while keeping every processed device tracked in Google Sheets.

---

# Requirements

* Java JDK 17 or newer
* Maven
* Google Sheets API credentials
* Slack Bot Token
* Slack App-Level Token (Socket Mode)
* Skyonics account credentials
* Internet connection

---

# Setup

1. Clone the repository.

2. Create a `.env` file in the project root.

3. Place `credentials.json` in the project root.

Your project structure should look similar to:

```text
ELD-Fixer/
│
├── src/
├── pom.xml
├── .env
├── credentials.json
└── README.md
```

4. Configure your `.env` file with all required values:

* Google Sheets configuration
* Slack tokens
* Spreadsheet ID
* Spreadsheet range

5. Install project dependencies:

```bash
mvn clean install
```

---

# Running the application

## From an IDE

Run:

```text
com.eldsolution.FirstScript
```

---

## From the terminal

From the project root execute:

```bash
mvn exec:java -Dexec.mainClass="com.eldsolution.FirstScript"
```

---

# Workflow

Whenever a Slack message containing an ELD serial number is received:

1. The application extracts the device ID.
2. Google Sheets is searched for the device.
3. If the device already exists:

   * The repair counter is incremented.
   * A confirmation reaction is added in Slack.
4. If the device does not exist:

   * Skyonics is queried for the latest BLE packets.
   * The current device state is analyzed.
   * If necessary, recovery commands are automatically sent.
   * The device is checked again.
   * If still unresolved, the device is added to the Google Sheet for tracking.
   * Slack reactions indicate the outcome of the operation.

---

# Slack Reactions

| Reaction | Meaning                                                 |
| -------- | ------------------------------------------------------- |
| 👀       | Device not found in the tracker. Investigation started. |
| 📗       | Device added to the sheet successfully.                 |
| ✅       | BLE communication verified successfully.                |
| ❌       | Engine is off or the device is unplugged.               |

---

# Technologies

* Java 17
* Maven
* Slack Bolt SDK
* Google Sheets API
* Jackson
* dotenv-java
* Java HTTP Client

---

# Notes

* The application uses Slack Socket Mode for real-time message processing.
* Google Sheets serves as the device tracking database.
* Skyonics APIs are used to retrieve BLE packet information and perform automated recovery actions.
* Duplicate Slack events are ignored to prevent processing the same message more than once.

---

# Support

If you encounter any issues or have questions, feel free to reach out.

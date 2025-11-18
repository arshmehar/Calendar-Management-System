# Calendar Application - USEME.md

## Overview

This Calendar Application can run in two modes:

1. **Interactive mode** - Allows users to enter commands directly in the console
2. **Headless mode** - Reads commands from a text file and executes them automatically

---

## Building the Application

To build the JAR file, run the following command from the project root directory:
```bash
./gradlew jar
```

This will create the JAR file in the `build/libs` directory.

**Example output location:**
```
build/libs/calendar-1.0.jar
```

---

## Running the Application

You can run the application using the `java -jar` command.

### 1. Interactive Mode

In this mode, the application waits for user input via the console.
```bash
java -jar build/libs/calendar-1.0.jar --mode interactive
```

You can then type commands such as:
```
create event Meeting from 2025-10-31T10:00 to 2025-10-31T11:00
print events on 2025-10-31
exit
```

---

### 2. Headless Mode

In this mode, the application reads commands from a file (e.g., `commands.txt`) and executes them without user interaction.
```bash
java -jar build/libs/calendar-1.0.jar --mode headless commands.txt
```

**Example `commands.txt`:**
```
create event Meeting from 2025-10-31T10:00 to 2025-10-31T11:00
create event Lunch from 2025-10-31T13:00 to 2025-10-31T14:00
print events on 2025-10-31
exit
```

---

## Notes

- The `--mode` argument is **case-insensitive**, so `--mode Interactive` or `--mode HEADLESS` will both work
- Ensure your command file (for headless mode) is in the same directory where you are running the program, or provide the relative or absolute path
- The last command in headless mode **must be `exit`**

---

## Example Commands Summary

| Mode        | Example Command                                                    |
|-------------|-------------------------------------------------------------------|
| Interactive | `java -jar build/libs/calendar-1.0.jar --mode interactive`        |
| Headless    | `java -jar build/libs/calendar-1.0.jar --mode headless commands.txt` |

---


---

## Example Session

### Interactive Mode Session
```
$ java -jar build/libs/calendar-1.0.jar --mode interactive
Welcome to Calendar Application!
Type 'exit' to quit.

> create event Meeting from 2025-10-31T10:00 to 2025-10-31T11:00
✓ Event created successfully

> create event Lunch from 2025-10-31T13:00 to 2025-10-31T14:00
✓ Event created successfully

> print events on 2025-10-31
Events on 2025-10-31:
  • Meeting: 10:00 - 11:00
  • Lunch: 13:00 - 14:00

> export cal calendar.csv
✓ Calendar exported successfully
File saved at: /absolute/path/to/calendar.csv

> exit
Goodbye!
```

### Headless Mode Session

**commands.txt:**
```
create event Meeting from 2025-10-31T10:00 to 2025-10-31T11:00
create event Lunch from 2025-10-31T13:00 to 2025-10-31T14:00
print events on 2025-10-31
export cal calendar.csv
exit
```

**Run:**
```bash
$ java -jar build/libs/calendar-1.0.jar --mode headless commands.txt
Event created successfully
Event created successfully
Events on 2025-10-31:
  • Meeting: 10:00 - 11:00
  • Lunch: 13:00 - 14:00
Calendar exported successfully
File saved at: /absolute/path/to/calendar.csv
```

## Design Changes Documentation
The design changes and improvements are documented in the DesignChanges.md file
in the res folder. 

---

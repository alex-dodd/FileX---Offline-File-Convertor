# PAT Report: Algorithms and Advanced Techniques

**Project:** FileX - Offline File Converter  
**Student:** Alex Dodd  
**Purpose:** Grade 11 IT School PAT (Practical Assessment Task)

---

## 1. Externally Sourced Code

| Code Snippet/Section | Source/Link | AI Assistance | % of Total Code |
|---------------------|-------------|---------------|-----------------|
| None | N/A | None | 0% |

**Explanation:** All code in this project was written by the student. The README mentions "Java Swing tutorials and resources" as design inspiration, but the actual implementation uses JavaFX (not Swing) and all code is original. Third-party libraries (Apache POI, PDFBox, zip4j, SQLite JDBC) are used via Maven dependencies but are not considered externally sourced code - they are standard industry libraries used through their APIs.

---

## 2. Explanation of Critical Algorithms

### Algorithm 1: File Conversion Routing and Format Detection

**Purpose:** Determines the appropriate conversion method based on source file extension and target format.

**Pseudocode:**
```
BEGIN convertFile(sourceFile, targetFile, targetFormat)
    GET sourceName from sourceFile (convert to lowercase)
    CONVERT targetFormat to lowercase
    
    IF sourceName ends with ".docx" AND targetFormat equals "pdf" THEN
        CALL convertDocxToPdf(sourceFile, targetFile)
    ELSE IF sourceName ends with ".pdf" AND targetFormat equals "docx" THEN
        CALL convertPdfToDocx(sourceFile, targetFile)
    ELSE IF sourceName ends with ".csv" AND targetFormat equals "xlsx" THEN
        CALL convertCsvToXlsx(sourceFile, targetFile)
    ELSE IF sourceName ends with ".xlsx" AND targetFormat equals "csv" THEN
        CALL convertXlsxToCsv(sourceFile, targetFile)
    ELSE IF sourceName is an image format AND targetFormat is an image format THEN
        CALL convertImage(sourceFile, targetFile, targetFormat)
    ELSE
        RETURN false (unsupported conversion)
    END IF
    
    RETURN true
EXCEPTION HANDLE
    DISPLAY error message
    RETURN false
END
```

**Explanation:** This algorithm acts as the main dispatcher for file conversions. It examines the file extension of the source file and matches it with the requested target format to route the conversion to the appropriate specialized handler. This ensures that only valid conversion combinations are attempted and provides clear error handling for unsupported formats.

**AI Assistance:** None - implemented independently.

---

### Algorithm 2: CSV to Excel Conversion with Row Processing

**Purpose:** Reads a CSV file line by line and creates an Excel spreadsheet with proper cell formatting.

**Pseudocode:**
```
BEGIN convertCsvToXlsx(sourceFile, targetFile)
    OPEN sourceFile for reading
    CREATE new Excel workbook
    CREATE new sheet named "Sheet1"
    
    SET rowNum to 0
    
    WHILE there are lines to read FROM sourceFile DO
        READ line from file
        SPLIT line by comma into data array
        CREATE new row at rowNum position
        
        FOR each item in data array DO
            CREATE cell at current position
            SET cell value to current item
        END FOR
        
        INCREMENT rowNum
    END WHILE
    
    WRITE workbook to targetFile
    CLOSE all files
END
```

**Explanation:** This algorithm processes CSV files by reading them line by line, splitting each line on commas, and writing the resulting data into Excel cells. It handles the row and column indexing automatically and ensures all data is properly transferred to the spreadsheet format.

**AI Assistance:** None - standard file processing logic.

---

### Algorithm 3: Database Record Insertion with Prepared Statements

**Purpose:** Safely inserts conversion history records into the SQLite database using parameterized queries.

**Pseudocode:**
```
BEGIN insertRecord(record)
    SET sql query to "INSERT INTO conversion_history(...) VALUES(?,?,?,?,?,?)"
    
    CONNECT to database
    PREPARE statement with sql query
    
    SET parameter 1 to record.sourcePath
    SET parameter 2 to record.targetPath
    SET parameter 3 to record.sourceFormat
    SET parameter 4 to record.targetFormat
    SET parameter 5 to record.success
    SET parameter 6 to record.timestamp
    
    EXECUTE statement
    
    CLOSE connection
EXCEPTION HANDLE
    DISPLAY error message
END
```

**Explanation:** This algorithm inserts conversion records into the database using prepared statements to prevent SQL injection. Each field from the ConversionRecord object is safely bound to a parameter placeholder, then the query is executed. This approach is more secure than string concatenation.

**AI Assistance:** None - standard database programming practice.

---

### Algorithm 4: Asynchronous File Conversion with Progress Tracking

**Purpose:** Performs file conversion on a background thread to prevent UI freezing while updating progress.

**Pseudocode:**
```
BEGIN handleConvertFile()
    GET source file path, target format, output location from UI
    
    VALIDATE inputs (check if empty or invalid)
    IF validation fails THEN
        DISPLAY error
        EXIT
    END IF
    
    SHOW progress bar
    SET status to "Converting..."
    
    CREATE background task:
        BEGIN task
            SET up source and target file paths
            CHECK if target file exists
            IF exists AND user doesn't confirm overwrite THEN
                CANCEL conversion
                EXIT task
            END IF
            
            CALL conversionHandler.convertFile()
        END task
    
    ON task success:
        HIDE progress bar
        SET status to "Conversion completed successfully!"
        IF logging enabled THEN
            SAVE conversion record to database
        END IF
    
    ON task failure:
        HIDE progress bar
        SET status to "Conversion failed"
        DISPLAY error dialog
    
    START background task in new thread
END
```

**Explanation:** This algorithm manages the user interface during file conversion by running the actual conversion process in a background thread. It validates user input, shows progress feedback, handles file overwrite confirmation, and logs successful conversions to the database. This prevents the UI from freezing during long conversion operations.

**AI Assistance:** None - implemented using JavaFX Task pattern.

---

### Algorithm 5: Settings Persistence Using Properties

**Purpose:** Loads and saves application settings to a properties file for persistence across sessions.

**Pseudocode:**
```
BEGIN loadSettings()
    SET settingsFile to "settings.properties"
    
    IF settingsFile exists THEN
        OPEN settingsFile for reading
        LOAD properties from file
        CLOSE file
    END IF
EXCEPTION HANDLE
    DISPLAY error message
END

BEGIN saveSettings()
    OPEN settingsFile for writing
    STORE properties to file with comment "FileX Application Settings"
    CLOSE file
EXCEPTION HANDLE
    DISPLAY error message
END

BEGIN getSetting(key, defaultValue)
    GET property value for key
    IF value is null or empty THEN
        RETURN defaultValue
    ELSE
        RETURN value
    END IF
END

BEGIN setSetting(key, value)
    SET property key to value
    CALL saveSettings()
END
```

**Explanation:** This algorithm manages application settings by reading from and writing to a properties file. When the application starts, settings are loaded from the file. When users change settings, they are immediately saved back to the file. This ensures settings persist between application sessions. Each setting has a default value that is used if no saved value exists.

**AI Assistance:** None - standard Java Properties API usage.

---

## 3. Advanced Techniques

### Technique 1: Multi-Threading with JavaFX Task

**Description:** Used JavaFX Task objects to perform file conversions and ZIP creation on background threads, preventing UI freezing during long operations.

**Pseudocode:**
```
BEGIN performFileConversion(sourceFile, targetFile, targetFormat)
    CREATE new Task:
        BEGIN call() method
            UPDATE progress to 0
            CALL conversionHandler.convertFile(sourceFile, targetFile, targetFormat)
            RETURN result
        END call
        
        BEGIN succeeded() method
            HIDE progress bar
            IF conversion successful THEN
                DISPLAY success message
                SAVE to history database
            ELSE
                DISPLAY failure message
            END IF
        END succeeded
        
        BEGIN failed() method
            HIDE progress bar
            DISPLAY error with exception message
        END failed
    
    BIND progress bar to task progress property
    SHOW progress bar
    START task in new thread
END
```

**Usage in Project:** Used in MainUIController for file conversion and ZIP creation operations to keep the UI responsive.

---

### Technique 2: Database Operations with SQLite and JDBC

**Description:** Used SQLite database to store conversion history records with full CRUD operations through JDBC.

**Pseudocode:**
```
BEGIN initialize()
    SET database URL to "jdbc:sqlite:conversion_history.db"
    CONNECT to database
    CREATE statement
    
    SET sql to "CREATE TABLE IF NOT EXISTS conversion_history (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        source_path TEXT NOT NULL,
        target_path TEXT NOT NULL,
        source_format TEXT NOT NULL,
        target_format TEXT NOT NULL,
        success BOOLEAN NOT NULL,
        timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
    )"
    
    EXECUTE sql
    CLOSE connection
END

BEGIN getAllRecords()
    CONNECT to database
    SET query to "SELECT * FROM conversion_history ORDER BY timestamp ASC"
    EXECUTE query and get results
    
    CREATE empty list for records
    FOR each row in results DO
        CREATE ConversionRecord from row data
        ADD record to list
    END FOR
    
    RETURN list
END
```

**Usage in Project:** DatabaseManager initializes the database schema on startup. ConversionHistoryDAO provides methods to insert, retrieve, and clear conversion records. Used throughout the application to maintain conversion history.

---

### Technique 3: Password-Protected ZIP with AES Encryption

**Description:** Implemented ZIP file creation with optional AES encryption using the zip4j library.

**Pseudocode:**
```
BEGIN createZip(sourceFolder, targetZipFile, password)
    CREATE new ZipFile object with targetZipFile
    CREATE new ZipParameters object
    
    IF password is not null or empty THEN
        SET parameters.encryptFiles to true
        SET parameters.encryptionMethod to AES
        SET zipFile password to password characters
    END IF
    
    ADD sourceFolder to zipFile with parameters
    RETURN true
EXCEPTION HANDLE
    DISPLAY error message
    RETURN false
END
```

**Usage in Project:** ZipHandler class provides ZIP creation functionality with optional password protection using AES encryption. Used in MainUIController when users want to compress folders with optional security.

---

### Technique 4: Singleton Design Pattern

**Description:** Implemented the Singleton pattern for SettingsManager to ensure only one instance manages application settings.

**Pseudocode:**
```
CLASS SettingsManager
    PRIVATE STATIC instance = null
    PRIVATE properties
    
    PRIVATE CONSTRUCTOR
        CALL loadSettings()
    END CONSTRUCTOR
    
    PUBLIC STATIC METHOD getInstance()
        IF instance is null THEN
            CREATE new SettingsManager instance
        END IF
        RETURN instance
    END METHOD
    
    PUBLIC METHOD getProperty(key)
        RETURN properties.get(key)
    END METHOD
    
    PUBLIC METHOD setProperty(key, value)
        SET properties.key to value
        CALL saveSettings()
    END METHOD
END CLASS
```

**Usage in Project:** SettingsManager uses Singleton pattern to ensure all parts of the application share the same settings instance. This prevents conflicts and ensures consistency when reading or writing settings.

---

### Technique 5: Observable Collections and Data Binding

**Description:** Used JavaFX ObservableList and FilteredList to automatically update the UI when data changes.

**Pseudocode:**
```
BEGIN setupHistoryTable()
    CREATE ObservableList from database records
    CREATE FilteredList wrapping ObservableList
    
    BIND table to FilteredList
    
    SET up filter predicate:
        BEGIN predicate(record)
            IF filter text is empty THEN
                RETURN true
            END IF
            
            IF record matches filter criteria THEN
                RETURN true
            ELSE
                RETURN false
            END IF
        END predicate
    
    WHEN filter text changes:
        UPDATE FilteredList predicate
        (Table automatically refreshes)
END
```

**Usage in Project:** HistoryUIController uses ObservableList and FilteredList to display conversion history. When the filter text changes or records are added, the table automatically updates without manual refresh code.

---

### Technique 6: Platform-Specific Thread Synchronization

**Description:** Used JavaFX Platform.runLater() combined with CountDownLatch to synchronize background threads with UI operations.

**Pseudocode:**
```
BEGIN confirmOverwrite(file)
    IF auto-overwrite setting enabled THEN
        RETURN true
    END IF
    
    CREATE CountDownLatch with count = 1
    CREATE AtomicBoolean for user response
    
    CALL Platform.runLater:
        BEGIN UI thread operation
            CREATE confirmation alert dialog
            SET dialog message
            SHOW dialog and wait for user
            
            IF user clicked OK THEN
                SET userResponse to true
            ELSE
                SET userResponse to false
            END IF
            
            COUNT DOWN latch
        END UI thread operation
    
    WAIT for latch (blocks until UI completes)
    RETURN userResponse value
END
```

**Usage in Project:** MainUIController uses this technique to show confirmation dialogs from background threads. The CountDownLatch ensures the background thread waits for user input from the UI thread before proceeding.

---

### Technique 7: Dynamic File Naming Convention System

**Description:** Implemented a flexible system to apply different naming conventions to converted files based on user settings.

**Pseudocode:**
```
BEGIN applyFileNamingConvention(baseFileName, extension)
    GET current naming convention setting
    
    CASE convention OF
        "Add timestamp":
            GET current date and time
            FORMAT as "dd_MM_yyyy-HH_mm_ss"
            RETURN baseFileName + "_" + formatted_datetime + extension
            
        "Add _converted suffix":
            RETURN baseFileName + "_converted" + extension
            
        "Keep original name":
        DEFAULT:
            RETURN baseFileName + extension
    END CASE
END
```

**Usage in Project:** SettingsManager provides this functionality to automatically apply naming conventions when saving converted files. Users can choose between keeping original names, adding timestamps, or adding a "converted" suffix.

---

## Summary

This PAT project demonstrates proficiency in file handling, database management, multi-threading, and user interface design. The application successfully implements various conversion algorithms while maintaining a responsive user experience through asynchronous operations. Advanced techniques such as encryption, design patterns, and data binding showcase understanding beyond basic syllabus requirements.

**Total Lines of Code:** Approximately 2,727 lines of Java code  
**External Code Percentage:** 0% (all original work using standard libraries)
